package com.example.jpatraining.problems;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Problem #14 — batch inserts. With {@code hibernate.jdbc.batch_size} (20, set in application.yml) and
 * {@code order_inserts}, many same-type inserts share one prepared statement executed as a JDBC batch,
 * so the prepared-statement count is far below the row count. (SEQUENCE id generation is what makes
 * this possible — IDENTITY would force a round trip per row and disable batching.)
 */
class BatchInsertProblemTest extends AbstractIntegrationTest {

    @Test
    void batchInserts_useFarFewerStatementsThanRows() {
        sql.reset();
        tx.executeWithoutResult(s -> {
            for (int i = 0; i < 20; i++) {
                em.persist(new Brand("BATCH-" + i));
            }
            em.flush();
        });
        long statements = sql.statementCount();

        assertTrue(statements < 20,
                "20 inserts should collapse to a handful of batched statements (was " + statements + ")");
    }
}
