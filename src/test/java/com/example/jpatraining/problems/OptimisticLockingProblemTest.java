package com.example.jpatraining.problems;

import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.OrderStatus;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Problem #15 — the lost update. Two transactions read the same row; without protection the second
 * commit silently overwrites the first. An {@code @Version} column makes the stale write fail loudly
 * (optimistic locking) instead of losing data.
 */
class OptimisticLockingProblemTest extends AbstractIntegrationTest {

    @Test
    void staleUpdate_failsWithOptimisticLock() {
        long orderId = tx.execute(s -> {
            Order order = new Order("ORD-OPTLOCK");
            em.persist(order);
            return order.getId();
        });

        // "User A" loads the order and keeps a copy (version 0), then steps away.
        Order staleCopy = tx.execute(s -> em.find(Order.class, orderId));

        // "User B" updates the same order in the meantime -> DB version 0 -> 1.
        tx.executeWithoutResult(s -> em.find(Order.class, orderId).setStatus(OrderStatus.PAID));

        // "User A" writes back the now-stale copy -> optimistic-lock failure (not a silent overwrite).
        Throwable thrown = assertThrows(Exception.class, () -> tx.executeWithoutResult(s -> {
            staleCopy.setStatus(OrderStatus.SHIPPED);
            em.merge(staleCopy);
            em.flush();
        }));

        boolean optimistic = false;
        for (Throwable c = thrown; c != null; c = c.getCause()) {
            String name = c.getClass().getSimpleName().toLowerCase();
            if (name.contains("optimisticlock") || name.contains("staleobjectstate")) {
                optimistic = true;
                break;
            }
        }
        assertTrue(optimistic, "expected an optimistic-lock failure, got: " + thrown);
    }

    @Test
    void freshUpdate_succeedsAndIncrementsVersion() {
        long orderId = tx.execute(s -> {
            Order order = new Order("ORD-OPTLOCK-OK");
            em.persist(order);
            return order.getId();
        });

        long before = tx.execute(s -> em.find(Order.class, orderId).getVersion());
        tx.executeWithoutResult(s -> em.find(Order.class, orderId).setStatus(OrderStatus.PAID));
        long after = tx.execute(s -> em.find(Order.class, orderId).getVersion());

        assertEquals(before + 1, after, "a successful update increments @Version");
    }
}
