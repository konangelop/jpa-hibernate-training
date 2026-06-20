package com.example.jpatraining.embeddables;

import com.example.jpatraining.common.Money;
import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.Payment;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Money} is an {@code @Embeddable} value object: its columns ({@code amount}, {@code currency})
 * live in the OWNER's table ({@code payment}). So loading the owner needs no join and no extra select,
 * and the value round-trips by value (not by identity).
 */
class MoneyEmbeddableTest extends AbstractIntegrationTest {

    @Test
    void embeddedMoney_roundTripsAndStaysInOwnerTable() {
        Long paymentId = tx.execute(s -> {
            Order order = new Order("ORD-MONEY-1");
            em.persist(order);
            Payment payment = new Payment(order, Money.of("99.90", "EUR"), "CARD");
            em.persist(payment);
            return payment.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Payment payment = em.find(Payment.class, paymentId);

            // The embedded columns are in the payment row itself: a single select, no separate
            // "money" table to join (there is none).
            sql.assertSelectCount(1);
            assertEquals(Money.of("99.90", "EUR"), payment.getAmount());
            assertEquals("EUR", payment.getAmount().getCurrency());
        });
    }
}
