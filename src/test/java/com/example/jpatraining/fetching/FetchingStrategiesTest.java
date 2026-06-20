package com.example.jpatraining.fetching;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.catalog.ProductRepository;
import com.example.jpatraining.catalog.Review;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.customer.Customer;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Four ways to load a lazy collection (Product.reviews) without N+1, each proven by counting SQL:
 * a JPQL {@code JOIN FETCH}, a Spring Data {@code @EntityGraph}, Hibernate {@code @BatchSize}, and
 * the two-step pattern for pagination (which {@code JOIN FETCH} + {@code setMaxResults} would
 * otherwise paginate in memory — Hibernate's HHH000104 warning).
 */
class FetchingStrategiesTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    /** Persists {@code productCount} products (each with {@code reviewsEach} reviews); returns their ids. */
    private List<Long> productsWithReviews(String suffix, int productCount, int reviewsEach) {
        return tx.execute(s -> {
            Brand brand = new Brand("B-" + suffix);
            Category category = new Category("C-" + suffix);
            em.persist(brand);
            em.persist(category);
            Customer author = new Customer("rev-" + suffix + "@example.com", "Rev");
            em.persist(author);
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < productCount; i++) {
                Product product = new Product("P-" + suffix + "-" + i, Money.of("1.00", "EUR"), brand, category);
                for (int j = 0; j < reviewsEach; j++) {
                    product.addReview(new Review(author, 5, "good"));
                }
                em.persist(product); // cascade persists the reviews
                ids.add(product.getId());
            }
            return ids;
        });
    }

    @Test
    void joinFetch_loadsAllReviewsInOneQuery() {
        List<Long> ids = productsWithReviews("JF", 3, 2);

        tx.executeWithoutResult(s -> {
            sql.reset();
            List<Product> products = em.createQuery(
                            "select distinct p from Product p left join fetch p.reviews where p.id in :ids",
                            Product.class)
                    .setParameter("ids", ids).getResultList();
            long afterQuery = sql.statementCount();
            for (Product p : products) {
                p.getReviews().size();
            }
            long afterNavigate = sql.statementCount();

            assertEquals(3, products.size());
            assertEquals(1, afterQuery, "a single JOIN FETCH query");
            assertEquals(0, afterNavigate - afterQuery, "reviews already fetched: no N+1");
        });
    }

    @Test
    void entityGraph_loadsReviewsWithoutNPlusOne() {
        List<Long> ids = productsWithReviews("EG", 3, 2);

        tx.executeWithoutResult(s -> {
            sql.reset();
            List<Product> products = productRepository.findByIdIn(ids);
            long afterQuery = sql.statementCount();
            for (Product p : products) {
                p.getReviews().size();
            }
            long afterNavigate = sql.statementCount();

            assertEquals(3, products.stream().map(Product::getId).distinct().count());
            assertEquals(0, afterNavigate - afterQuery, "reviews fetched via the entity graph: no N+1");
        });
    }

    @Test
    void batchSize_loadsManyCollectionsInOneBatchedQuery() {
        List<Long> ids = productsWithReviews("BS", 3, 2);

        tx.executeWithoutResult(s -> {
            List<Product> products = em.createQuery(
                            "select p from Product p where p.id in :ids", Product.class)
                    .setParameter("ids", ids).getResultList();
            sql.reset();
            for (Product p : products) {
                p.getReviews().size(); // @BatchSize(10) batches all three collection loads
            }
            assertEquals(1, sql.statementCount(),
                    "@BatchSize loads all 3 review collections in one batched select (vs 3 without it)");
        });
    }

    @Test
    void pagination_twoStep_pagesAtTheSqlLevelWithoutNPlusOne() {
        List<Long> ids = productsWithReviews("PAG", 5, 2);

        tx.executeWithoutResult(s -> {
            sql.reset();
            // Step 1 — page the IDs at the SQL level (no collection fetch, so LIMIT is honoured).
            List<Long> pageIds = em.createQuery(
                            "select p.id from Product p where p.id in :ids order by p.id", Long.class)
                    .setParameter("ids", ids).setMaxResults(2).getResultList();
            // Step 2 — fetch exactly that page's entities together with their reviews.
            List<Product> page = em.createQuery(
                            "select distinct p from Product p left join fetch p.reviews where p.id in :pageIds order by p.id",
                            Product.class)
                    .setParameter("pageIds", pageIds).getResultList();
            long afterQueries = sql.statementCount();
            for (Product p : page) {
                p.getReviews().size();
            }
            long afterNavigate = sql.statementCount();

            assertEquals(2, page.size(), "exactly the requested page size");
            assertEquals(2, afterQueries, "two queries: page the ids, then fetch with reviews");
            assertEquals(0, afterNavigate - afterQueries, "reviews fetched: no N+1 on the page");
        });
    }
}
