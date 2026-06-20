package com.example.jpatraining.manytomany;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.catalog.Tag;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Product <-> Tag}: a pure bidirectional ManyToMany over a {@code @JoinTable} ({@code product_tag}),
 * using {@code Set}. Product is the owning side (declares the join table); {@code Tag.products} is
 * {@code mappedBy}. Both collections are lazy and load with a single select.
 */
class PureManyToManyTest extends AbstractIntegrationTest {

    /** Returns {productId, firstTagId}. */
    private long[] productWithTwoTags(String suffix) {
        return tx.execute(s -> {
            Brand brand = new Brand("B-" + suffix);
            Category category = new Category("C-" + suffix);
            em.persist(brand);
            em.persist(category);
            Tag t1 = new Tag("tag-" + suffix + "-1");
            Tag t2 = new Tag("tag-" + suffix + "-2");
            em.persist(t1);
            em.persist(t2);
            Product product = new Product("P-" + suffix, Money.of("1.00", "EUR"), brand, category);
            product.addTag(t1);
            product.addTag(t2);
            em.persist(product); // owning side -> writes the product_tag rows
            return new long[]{product.getId(), t1.getId()};
        });
    }

    @Test
    void owningSide_tagsAreLazyAndLoadInOneSelect() {
        long[] ids = productWithTwoTags("OWN");

        tx.executeWithoutResult(s -> {
            sql.reset();
            Product product = em.find(Product.class, ids[0]);
            long afterLoad = sql.statementCount();
            int tagCount = product.getTags().size();
            long afterTouch = sql.statementCount();

            assertEquals(1, afterLoad, "product loads in one select; tags are a lazy set");
            assertEquals(2, tagCount);
            assertEquals(1, afterTouch - afterLoad, "tags load with one select (join product_tag + tag)");
        });
    }

    @Test
    void inverseSide_productsAreLazyAndLoadInOneSelect() {
        long[] ids = productWithTwoTags("INV");

        tx.executeWithoutResult(s -> {
            sql.reset();
            Tag tag = em.find(Tag.class, ids[1]);
            long afterLoad = sql.statementCount();
            int productCount = tag.getProducts().size();
            long afterTouch = sql.statementCount();

            assertEquals(1, afterLoad);
            assertEquals(1, productCount);
            assertEquals(1, afterTouch - afterLoad, "products load with one select from the inverse side");
        });
    }

    @Test
    void set_ignoresDuplicateLinks() {
        tx.executeWithoutResult(s -> {
            Brand brand = new Brand("B-DEDUP");
            Category category = new Category("C-DEDUP");
            em.persist(brand);
            em.persist(category);
            Tag tag = new Tag("tag-DEDUP");
            em.persist(tag);
            Product product = new Product("P-DEDUP", Money.of("1.00", "EUR"), brand, category);

            product.addTag(tag);
            product.addTag(tag); // same tag again

            assertEquals(1, product.getTags().size(), "a Set collapses the duplicate link");
        });
    }
}
