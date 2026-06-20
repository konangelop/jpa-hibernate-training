package com.example.jpatraining.support;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Counts the SQL Hibernate actually issues, so concept/problem tests can assert on it.
 *
 * <p>Backed by Hibernate {@link Statistics} (enabled via {@code hibernate.generate_statistics=true}).
 * The key primitive is {@link Statistics#getPrepareStatementCount()} — the number of JDBC prepared
 * statements, i.e. the truest "SQL hit the database" count. It is what catches N+1: each lazy load
 * is another prepared statement. ({@code getQueryExecutionCount()} only counts HQL/criteria/native
 * query executions and does <em>not</em> see lazy-load selects, so it is the wrong primitive here.)
 *
 * <p>Usage: call {@link #reset()} immediately before the code under test, then assert.
 */
@Component
public class SqlCounter {

    private final Statistics statistics;

    public SqlCounter(EntityManagerFactory entityManagerFactory) {
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    /** Reset all counters. Call immediately before the code under test. */
    public void reset() {
        statistics.clear();
    }

    /** Total JDBC prepared statements executed since {@link #reset()}. */
    public long statementCount() {
        return statistics.getPrepareStatementCount();
    }

    public long entityLoadCount() {
        return statistics.getEntityLoadCount();
    }

    public long collectionLoadCount() {
        return statistics.getCollectionLoadCount();
    }

    public long insertCount() {
        return statistics.getEntityInsertCount();
    }

    /**
     * Assert the number of SQL statements issued since {@link #reset()}. In a read-only measured
     * block this equals the SELECT count — what turns "N+1" into a concrete pass/fail number.
     */
    public void assertSelectCount(long expected) {
        assertEquals(expected, statistics.getPrepareStatementCount(),
                () -> "Expected " + expected + " SQL statement(s) but Hibernate issued "
                        + statistics.getPrepareStatementCount());
    }
}
