package com.example.jpatraining.manytomany;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.customer.Customer;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Customer -> wishlist} ({@code Set<Product>}): a unidirectional ManyToMany over a
 * {@code @JoinTable} ({@code customer_wishlist}). Product has no back-reference. The wishlist is lazy
 * and loads with a single select.
 */
class UnidirectionalManyToManyTest extends AbstractIntegrationTest {

    @Test
    void wishlist_isLazyAndLoadsInOneSelect() {
        Long customerId = tx.execute(s -> {
            Brand brand = new Brand("B-WISH");
            Category category = new Category("C-WISH");
            em.persist(brand);
            em.persist(category);
            Product p1 = new Product("P-WISH-1", Money.of("1.00", "EUR"), brand, category);
            Product p2 = new Product("P-WISH-2", Money.of("2.00", "EUR"), brand, category);
            em.persist(p1);
            em.persist(p2);

            Customer customer = new Customer("wishlist@example.com", "Wendy");
            customer.addToWishlist(p1);
            customer.addToWishlist(p2);
            em.persist(customer); // owning side -> writes the customer_wishlist rows
            return customer.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Customer customer = em.find(Customer.class, customerId);
            long afterLoad = sql.statementCount();
            int wishlistSize = customer.getWishlist().size();
            long afterTouch = sql.statementCount();

            assertEquals(2, wishlistSize);
            assertEquals(1, afterTouch - afterLoad,
                    "wishlist loads with one select (join customer_wishlist + product)");
        });
    }
}
