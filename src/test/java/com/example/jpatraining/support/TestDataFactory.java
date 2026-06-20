package com.example.jpatraining.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * Thin helper for arranging persisted entities in tests. Entity-specific builders are added per
 * concept in later passes; for now it offers the primitives every test needs: persist some
 * entities, then flush + clear so subsequent reads actually hit the database.
 *
 * <p>Its methods must be called inside a transaction (e.g. within {@code tx.execute(...)}), since
 * the {@link EntityManager} is transaction-scoped.
 */
@Component
public class TestDataFactory {

    @PersistenceContext
    private EntityManager em;

    public void persist(Object... entities) {
        for (Object entity : entities) {
            em.persist(entity);
        }
    }

    /** Flush pending writes and clear the persistence context (empties the first-level cache). */
    public void flushAndClear() {
        em.flush();
        em.clear();
    }
}
