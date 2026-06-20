package com.example.jpatraining.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Base class for all integration tests.
 *
 * <p><strong>Deliberately NOT {@code @Transactional}.</strong> Query-count and lazy-loading demos
 * require explicit transaction boundaries and a cleared persistence context between <em>arrange</em>
 * and <em>act</em> — otherwise the first-level cache hides the very N+1 /
 * {@code LazyInitializationException} behaviour this project teaches. Drive transactions explicitly
 * via {@link #tx}, and clear the persistence context with {@link TestDataFactory#flushAndClear()}.
 *
 * <p>A single Testcontainers Postgres is shared across the whole suite: every test extends this
 * class, so they share one Spring application context (context cache) and therefore one container.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(ContainerConfig.class)
public abstract class AbstractIntegrationTest {

    @PersistenceContext
    protected EntityManager em;

    @Autowired
    protected TransactionTemplate tx;

    @Autowired
    protected SqlCounter sql;

    @Autowired
    protected TestDataFactory data;
}
