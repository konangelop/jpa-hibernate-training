package com.example.jpatraining.manytoone_onetomany;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.OrderItem;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Order <-> OrderItem}: parent–child with {@code cascade = ALL} and
 * {@code orphanRemoval = true}. Persisting the order cascades to its items; removing an item from
 * the collection deletes that row on flush.
 */
class CascadeOrphanRemovalTest extends AbstractIntegrationTest {

    private long orderWithTwoItems(String orderNumber) {
        return tx.execute(s -> {
            Brand brand = new Brand("B-" + orderNumber);
            Category category = new Category("C-" + orderNumber);
            em.persist(brand);
            em.persist(category);
            Product product = new Product("P-" + orderNumber, Money.of("5.00", "EUR"), brand, category);
            em.persist(product);

            Order order = new Order(orderNumber);
            order.addItem(new OrderItem(product, 1, Money.of("5.00", "EUR")));
            order.addItem(new OrderItem(product, 2, Money.of("5.00", "EUR")));
            em.persist(order); // cascade = ALL persists both items
            return order.getId();
        });
    }

    @Test
    void cascade_persistingOrderPersistsItems() {
        long orderId = orderWithTwoItems("ORD-CASC");

        tx.executeWithoutResult(s -> {
            Order order = em.find(Order.class, orderId);
            assertEquals(2, order.getItems().size(), "both items were cascade-persisted with the order");
        });
    }

    @Test
    void orphanRemoval_removingItemFromCollectionDeletesIt() {
        long orderId = orderWithTwoItems("ORD-ORPHAN");

        tx.executeWithoutResult(s -> {
            Order order = em.find(Order.class, orderId);
            order.getItems().size(); // initialize the collection
            sql.reset();
            order.removeItem(order.getItems().get(0)); // orphan -> deleted on flush
            em.flush();
            assertEquals(1, sql.deleteCount(), "the orphaned order item is deleted");
        });

        tx.executeWithoutResult(s -> {
            Order order = em.find(Order.class, orderId);
            assertEquals(1, order.getItems().size(), "only one item remains in the database");
        });
    }
}
