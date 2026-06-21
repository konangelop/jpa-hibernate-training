package com.example.jpatraining.problems;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.catalog.ProductRepository;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Problem #12 — over-fetching. When a screen needs only a few fields, loading whole entities (and
 * their associations) is wasteful and invites N+1. Two projection styles select exactly the columns
 * needed in a single query: a <strong>constructor</strong> projection (JPQL {@code select new ...})
 * and a Spring Data <strong>interface</strong> projection.
 */
class DtoProjectionProblemTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

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

    @Test
    void interfaceProjection_isASingleNarrowedQuery() {
        tx.executeWithoutResult(s -> {
            Brand brand = new Brand("IFP-brand");
            Category category = new Category("IFP-cat");
            em.persist(brand);
            em.persist(category);
            for (int i = 0; i < 3; i++) {
                em.persist(new Product("IFP-" + i, Money.of("9.99", "EUR"), brand, category));
            }
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            List<ProductView> views = productRepository.findByNameStartingWith("IFP-", ProductView.class);

            assertEquals(3, views.size());
            for (ProductView view : views) {
                assertTrue(view.getName().startsWith("IFP-"));
                assertEquals(new BigDecimal("9.99"), view.getPrice().getAmount());
            }
            assertEquals(1, sql.statementCount(),
                    "a closed interface projection issues one column-narrowed query — no entities, no N+1");
        });
    }
}
