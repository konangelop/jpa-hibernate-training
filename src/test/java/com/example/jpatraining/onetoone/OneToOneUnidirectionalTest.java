package com.example.jpatraining.onetoone;

import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.Shipment;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Order -> Shipment}: unidirectional OneToOne with the FK on the OWNING side
 * ({@code orders.shipment_id}). The owning side honours {@code LAZY}: loading the Order yields a
 * Shipment proxy, and no select is issued for it until the proxy is actually touched.
 */
class OneToOneUnidirectionalTest extends AbstractIntegrationTest {

    @Test
    void owningSideLazy_shipmentLoadedOnlyWhenTouched() {
        Long orderId = tx.execute(s -> {
            Shipment shipment = new Shipment("UPS", "1Z999AA");
            Order order = new Order("ORD-UNI-1");
            order.setShipment(shipment);
            em.persist(shipment); // no cascade Order->Shipment, so persist the target first
            em.persist(order);
            return order.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Order order = em.find(Order.class, orderId);
            long afterLoad = sql.statementCount();

            // Owning-side OneToOne is genuinely lazy here: shipment is still a proxy.
            String carrier = order.getShipment().getCarrier();
            long afterTouch = sql.statementCount();

            assertEquals("UPS", carrier);
            assertEquals(1, afterTouch - afterLoad,
                    "touching the lazy shipment proxy should trigger exactly one extra select");
        });
    }
}
