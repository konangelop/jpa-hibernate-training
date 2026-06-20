package com.example.jpatraining.problems;

import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.Shipment;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Problem #9 — missing cascade. {@code Order -> Shipment} has no cascade (shipments have their own
 * lifecycle), so persisting an order that points at a brand-new (transient) shipment fails at flush
 * with a transient-object error. The fix is to persist the shipment first (or add a cascade if the
 * child truly belongs to the parent — as {@code Order -> OrderItem} does).
 */
class CascadeProblemTest extends AbstractIntegrationTest {

    @Test
    void missingCascade_persistingParentWithTransientChild_throws() {
        assertThrows(Exception.class, () -> tx.executeWithoutResult(s -> {
            Order order = new Order("ORD-TRANSIENT");
            order.setShipment(new Shipment("UPS", "T-1")); // transient; Order->Shipment has no cascade
            em.persist(order); // fails at flush: references an unsaved transient Shipment
        }));
    }
}
