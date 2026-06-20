package com.example.jpatraining.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves the whole foundation works end-to-end:
 * <ul>
 *   <li>the Spring Boot context loads against a Testcontainers Postgres ({@code @ServiceConnection});</li>
 *   <li>the Hibernate-backed {@link SqlCounter} actually sees and counts SQL.</li>
 * </ul>
 * Green here means later passes can build concept/problem tests on this harness with confidence.
 */
class HarnessSmokeTest extends AbstractIntegrationTest {

    @Test
    void contextLoadsAndCounterCountsSql() {
        sql.reset();

        Object result = tx.execute(status ->
                em.createNativeQuery("select 1").getSingleResult());

        assertEquals(1, ((Number) result).intValue());
        // The single native query is exactly one JDBC prepared statement: proves the counter is wired.
        sql.assertSelectCount(1);
    }
}
