package com.example.jpatraining.manytomany;

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
 * {@code Order <-> Product} as a ManyToMany resolved through the join ENTITY {@link OrderItem}.
 * Unlike a pure {@code @ManyToMany}, the link itself carries columns ({@code quantity}, an embedded
 * {@code unitPrice}) — which is exactly why it must be modelled as an entity. Navigating to the far
 * side (products) is N+1 unless fetched explicitly.
 */
class JoinEntityManyToManyTest extends AbstractIntegrationTest {

    private long orderWithTwoProducts(String orderNumber) {
        return tx.execute(s -> {
            Brand brand = new Brand("B-" + orderNumber);
            Category category = new Category("C-" + orderNumber);
            em.persist(brand);
            em.persist(category);
            Product p1 = new Product("P-" + orderNumber + "-1", Money.of("5.00", "EUR"), brand, category);
            Product p2 = new Product("P-" + orderNumber + "-2", Money.of("7.00", "EUR"), brand, category);
            em.persist(p1);
            em.persist(p2);

            Order order = new Order(orderNumber);
            order.addItem(new OrderItem(p1, 2, Money.of("5.00", "EUR")));
            order.addItem(new OrderItem(p2, 1, Money.of("7.00", "EUR")));
            em.persist(order); // cascade ALL persists the items
            return order.getId();
        });
    }

    @Test
    void joinEntity_carriesLinkAttributes() {
        long orderId = orderWithTwoProducts("ORD-JE-ATTR");

        tx.executeWithoutResult(s -> {
            Order order = em.find(Order.class, orderId);
            assertEquals(2, order.getItems().size());
            int totalQuantity = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
            assertEquals(3, totalQuantity, "the join entity carries quantity (2 + 1)");
        });
    }

    @Test
    void navigatingToProducts_isNPlusOne_fixedWithJoinFetch() {
        long orderId = orderWithTwoProducts("ORD-JE-FETCH");

        // BAD: the items load, then each item's product is a separate select.
        tx.executeWithoutResult(s -> {
            Order order = em.find(Order.class, orderId);
            order.getItems().size(); // load the items collection
            sql.reset();
            for (OrderItem item : order.getItems()) {
                item.getProduct().getName();
            }
            assertEquals(2, sql.statementCount(), "one select per product — N+1 across the join entity");
        });

        // GOOD: fetch the items and their products in a single query.
        tx.executeWithoutResult(s -> {
            sql.reset();
            Order order = em.createQuery(
                            "select o from Order o join fetch o.items i join fetch i.product where o.id = :id",
                            Order.class)
                    .setParameter("id", orderId)
                    .getSingleResult();
            long afterQuery = sql.statementCount();
            for (OrderItem item : order.getItems()) {
                item.getProduct().getName();
            }
            long afterNavigate = sql.statementCount();

            assertEquals(2, order.getItems().size());
            assertEquals(0, afterNavigate - afterQuery, "items and products fetched together: no N+1");
        });
    }
}
