package com.example.jpatraining.problems;

import com.example.jpatraining.customer.Customer;
import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Problem #7 — updating the wrong side of a bidirectional association. Only the OWNING side
 * ({@code Order.customer}) controls the FK column; mutating the inverse collection alone persists
 * nothing. The fix is an owning-side-aware helper ({@code Customer.addOrder}).
 */
class WrongOwningSideProblemTest extends AbstractIntegrationTest {

    @Test
    void updatingOnlyInverseSide_doesNotWriteTheForeignKey() {
        long orderId = tx.execute(s -> {
            Customer customer = new Customer("wrong-owning@example.com", "X");
            em.persist(customer);
            Order order = new Order("ORD-WRONGOWN");
            customer.getOrders().add(order); // WRONG: inverse side only; owning side (order.customer) stays null
            em.persist(order);
            return order.getId();
        });

        tx.executeWithoutResult(s -> {
            Order order = em.find(Order.class, orderId);
            assertNull(order.getCustomer(), "the inverse side does not control the FK column");
        });
    }

    @Test
    void usingTheOwningSideHelper_writesTheForeignKey() {
        long orderId = tx.execute(s -> {
            Customer customer = new Customer("right-owning@example.com", "Y");
            em.persist(customer);
            Order order = new Order("ORD-RIGHTOWN");
            customer.addOrder(order); // helper sets the owning side: order.setCustomer(customer)
            em.persist(order);
            return order.getId();
        });

        tx.executeWithoutResult(s -> {
            Order order = em.find(Order.class, orderId);
            assertNotNull(order.getCustomer(), "the owning-side helper wrote the FK");
        });
    }
}
