package com.example.jpatraining.problems;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.OrderItem;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Problem #2 — {@code LazyInitializationException}. Touching a lazy association after the persistence
 * context has closed fails. (This is also why the project keeps {@code spring.jpa.open-in-view=false}:
 * OSIV would hide the bug by keeping the session open across the whole web request.)
 */
class LazyInitializationProblemTest extends AbstractIntegrationTest {

    private long orderWithItem(String orderNumber) {
        return tx.execute(s -> {
            Brand brand = new Brand("LIE-" + orderNumber);
            Category category = new Category("LIE-" + orderNumber);
            em.persist(brand);
            em.persist(category);
            Product product = new Product("LIE-" + orderNumber, Money.of("1.00", "EUR"), brand, category);
            em.persist(product);
            Order order = new Order(orderNumber);
            order.addItem(new OrderItem(product, 1, Money.of("1.00", "EUR")));
            em.persist(order);
            return order.getId();
        });
    }

    @Test
    void accessingLazyAssociationAfterTxCloses_throws() {
        long orderId = orderWithItem("ORD-LIE-BAD");

        Order detached = tx.execute(s -> em.find(Order.class, orderId));
        // The transaction (and persistence context) has closed; items were never initialized.
        assertThrows(LazyInitializationException.class, () -> detached.getItems().size());
    }

    @Test
    void initializingInsideTx_thenUsingAfter_works() {
        long orderId = orderWithItem("ORD-LIE-GOOD");

        Order loaded = tx.execute(s -> {
            Order order = em.find(Order.class, orderId);
            order.getItems().size(); // initialize within the session (or use JOIN FETCH)
            return order;
        });
        assertEquals(1, loaded.getItems().size()); // safe after detach
    }
}
