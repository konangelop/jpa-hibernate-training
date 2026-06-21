package com.example.jpatraining.embeddables;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code @ElementCollection} maps a collection of <em>basic</em> values ({@code Product.imageUrls})
 * to its own table ({@code product_image}, FK back to product) — not a separate entity. It is lazy
 * by default and loads with a single select.
 */
class ElementCollectionTest extends AbstractIntegrationTest {

    @Test
    void imageUrls_areLazyAndLoadInOneSelect() {
        Long productId = tx.execute(s -> {
            Brand brand = new Brand("EC-brand");
            Category category = new Category("EC-cat");
            em.persist(brand);
            em.persist(category);
            Product product = new Product("EC-prod", Money.of("1.00", "EUR"), brand, category);
            product.addImageUrl("https://img.example/1.png");
            product.addImageUrl("https://img.example/2.png");
            em.persist(product); // element collection is written with the owner
            return product.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Product product = em.find(Product.class, productId);
            long afterLoad = sql.statementCount();
            int imageCount = product.getImageUrls().size();
            long afterTouch = sql.statementCount();

            assertEquals(1, afterLoad, "product loads in one select; imageUrls is a lazy element collection");
            assertEquals(2, imageCount);
            assertEquals(1, afterTouch - afterLoad, "imageUrls load with one select from product_image");
        });
    }
}
