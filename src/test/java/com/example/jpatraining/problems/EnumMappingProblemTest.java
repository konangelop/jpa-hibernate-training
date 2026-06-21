package com.example.jpatraining.problems;

import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.OrderStatus;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Problem #16 — the {@code @Enumerated} mapping choice. {@code STRING} stores the constant <em>name</em>
 * (stable). {@code ORDINAL} stores the <em>position</em> as an integer — fragile, because reordering or
 * inserting enum constants silently remaps every existing row. Both are read back from the raw column
 * via native SQL.
 */
class EnumMappingProblemTest extends AbstractIntegrationTest {

    @Test
    void stringEnum_storesTheConstantName() {
        long orderId = tx.execute(s -> {
            Order order = new Order("ORD-ENUM-STR");
            order.setStatus(OrderStatus.PAID);
            em.persist(order);
            return order.getId();
        });

        tx.executeWithoutResult(s -> {
            Object raw = em.createNativeQuery("select status from orders where id = :id")
                    .setParameter("id", orderId).getSingleResult();
            assertEquals("PAID", raw.toString(), "@Enumerated(STRING) stores the constant name");
        });
    }

    @Test
    void ordinalEnum_storesAFragilePosition() {
        long id = tx.execute(s -> {
            OrdinalEnumEntity entity = new OrdinalEnumEntity(OrderStatus.PAID);
            em.persist(entity);
            return entity.getId();
        });

        tx.executeWithoutResult(s -> {
            Object raw = em.createNativeQuery("select status from ordinal_enum_entity where id = :id")
                    .setParameter("id", id).getSingleResult();
            assertEquals(1, ((Number) raw).intValue(),
                    "@Enumerated(ORDINAL) stores the position (PAID = index 1) — reordering would corrupt it");
        });
    }
}
