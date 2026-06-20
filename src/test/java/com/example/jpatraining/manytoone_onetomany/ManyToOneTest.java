package com.example.jpatraining.manytoone_onetomany;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Product -> Brand}: unidirectional ManyToOne. The FK ({@code brand_id}) is on the product
 * (the many side is always the owning side). Like the owning side of a OneToOne, it honours LAZY:
 * the brand is a proxy until touched.
 */
class ManyToOneTest extends AbstractIntegrationTest {

    @Test
    void manyToOne_isLazyProxyUntilTouched() {
        Long productId = tx.execute(s -> {
            Brand brand = new Brand("Acme-M2O");
            Category category = new Category("Cat-M2O");
            em.persist(brand);
            em.persist(category);
            Product product = new Product("Widget", Money.of("9.99", "EUR"), brand, category);
            em.persist(product);
            return product.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Product product = em.find(Product.class, productId);
            long afterLoad = sql.statementCount();

            String brandName = product.getBrand().getName();
            long afterTouch = sql.statementCount();

            assertEquals(1, afterLoad, "loading the product is a single select; brand and category are proxies");
            assertEquals("Acme-M2O", brandName);
            assertEquals(1, afterTouch - afterLoad, "touching the lazy brand triggers exactly one select");
        });
    }
}
