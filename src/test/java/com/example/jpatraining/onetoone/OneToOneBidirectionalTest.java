package com.example.jpatraining.onetoone;

import com.example.jpatraining.common.Money;
import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.Payment;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@code Order <-> Payment}: bidirectional OneToOne. {@link Payment} is the OWNING side (holds
 * {@code order_id}); {@link Order} is the inverse side ({@code mappedBy = "order"}).
 *
 * <ul>
 *   <li><b>Owning side</b> honours LAZY: the FK is local, so {@code order} is a proxy until touched.</li>
 *   <li><b>Inverse side</b> is the classic OneToOne limitation: Hibernate must issue a select to
 *       learn whether the inverse is null or present, so it is resolved <em>during the load</em>
 *       even though declared LAZY — touching it later adds no further select.</li>
 * </ul>
 */
class OneToOneBidirectionalTest extends AbstractIntegrationTest {

    @Test
    void owningSide_orderIsLazyProxyUntilTouched() {
        Long paymentId = tx.execute(s -> {
            Order order = new Order("ORD-BIDI-OWN");
            em.persist(order);
            Payment payment = new Payment(order, Money.of("50.00", "EUR"), "CARD");
            em.persist(payment);
            return payment.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Payment payment = em.find(Payment.class, paymentId);
            long afterLoad = sql.statementCount();

            String number = payment.getOrder().getOrderNumber();
            long afterTouch = sql.statementCount();

            assertEquals("ORD-BIDI-OWN", number);
            assertEquals(1, afterTouch - afterLoad,
                    "owning-side order is a lazy proxy, initialized with one select when touched");
        });
    }

    @Test
    void inverseSide_paymentResolvedDuringLoadDespiteLazy() {
        Long orderId = tx.execute(s -> {
            Order order = new Order("ORD-BIDI-INV");
            em.persist(order);
            Payment payment = new Payment(order, Money.of("75.00", "EUR"), "CARD");
            em.persist(payment);
            return order.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Order order = em.find(Order.class, orderId);
            long afterLoad = sql.statementCount();

            // We never asked for it, yet the inverse association is already resolved.
            assertNotNull(order.getPayment());
            long afterTouchInverse = sql.statementCount();

            assertEquals(0, afterTouchInverse - afterLoad,
                    "inverse payment was already resolved during find(), so touching it adds no select");
        });
    }
}
