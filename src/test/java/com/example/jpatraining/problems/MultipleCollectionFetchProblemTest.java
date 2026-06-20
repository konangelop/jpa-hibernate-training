package com.example.jpatraining.problems;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Problem #3 — fetching multiple {@code List} (bag) collections in one query throws
 * {@code MultipleBagFetchException}. Category deliberately has two bags ({@code children},
 * {@code products}) to make it reproducible. The fix shown here is to load each collection in a
 * separate query (other options: use {@code Set}, or {@code @BatchSize}).
 *
 * <p>Related: the cartesian-product trap (#4). Fetching two collections at once makes the database
 * return M×N rows. In Hibernate 6+ the result's <em>root entities are auto-deduplicated</em> (so the
 * old "duplicate rows in the List" correctness bug is gone), but the wasted M×N fetch remains a
 * performance trap — so the same advice applies: one collection per query, or {@code @BatchSize}.
 */
class MultipleCollectionFetchProblemTest extends AbstractIntegrationTest {

    @Test
    void fetchingTwoListCollections_throwsMultipleBagFetchException() {
        Throwable thrown = assertThrows(Exception.class, () ->
                em.createQuery(
                        "select c from Category c left join fetch c.children left join fetch c.products",
                        Category.class).getResultList());

        while (thrown != null && !thrown.getClass().getSimpleName().contains("MultipleBagFetch")) {
            thrown = thrown.getCause();
        }
        assertNotNull(thrown, "expected a MultipleBagFetchException in the cause chain");
    }

    @Test
    void separateQueries_loadBothListsWithoutTheException() {
        long categoryId = tx.execute(s -> {
            Category parent = new Category("MBF-parent");
            em.persist(parent);
            Category child = new Category("MBF-child");
            parent.addChild(child);
            em.persist(child);
            Brand brand = new Brand("MBF-brand");
            em.persist(brand);
            Product product = new Product("MBF-prod", Money.of("1.00", "EUR"), brand, parent);
            em.persist(product);
            return parent.getId();
        });

        tx.executeWithoutResult(s -> {
            Category category = em.createQuery(
                            "select c from Category c left join fetch c.children where c.id = :id", Category.class)
                    .setParameter("id", categoryId).getSingleResult();
            em.createQuery(
                            "select c from Category c left join fetch c.products where c.id = :id", Category.class)
                    .setParameter("id", categoryId).getSingleResult();
            // same managed instance — both collections are now initialized
            assertEquals(1, category.getChildren().size());
            assertEquals(1, category.getProducts().size());
        });
    }
}
