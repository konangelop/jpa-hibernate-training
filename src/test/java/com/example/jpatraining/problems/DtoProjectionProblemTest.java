package com.example.jpatraining.problems;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Problem #12 — over-fetching. When a screen needs only a few fields, loading whole entities (and
 * their associations) is wasteful and invites N+1. A constructor (DTO) projection selects exactly
 * the columns needed in a single query.
 */
class DtoProjectionProblemTest extends AbstractIntegrationTest {

    @Test
    void constructorProjection_isASingleQueryWithNoEntities() {
        tx.executeWithoutResult(s -> {
            Brand brand = new Brand("DTO-brand");
            Category category = new Category("DTO-cat");
            em.persist(brand);
            em.persist(category);
            for (int i = 0; i < 3; i++) {
                em.persist(new Product("DTO-" + i, Money.of("9.99", "EUR"), brand, category));
            }
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            List<ProductSummary> summaries = em.createQuery(
                    "select new com.example.jpatraining.problems.ProductSummary(p.name, p.price.amount) "
                            + "from Product p where p.name like 'DTO-%'", ProductSummary.class)
                    .getResultList();

            assertEquals(3, summaries.size());
            assertEquals(1, sql.statementCount(),
                    "a constructor projection is one query selecting only the needed columns — no entities, no N+1");
        });
    }
}
