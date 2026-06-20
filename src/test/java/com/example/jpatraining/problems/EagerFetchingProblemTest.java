package com.example.jpatraining.problems;

import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Problem #5 — the EAGER pitfall. {@code @ManyToOne}/{@code @OneToOne} default to EAGER in the spec.
 * EAGER does NOT help queries: a JPQL {@code select} loads each row's eager association with a
 * secondary select — an N+1 you can't turn off at the call site. That's why this project maps every
 * association LAZY and fetches per query instead.
 */
class EagerFetchingProblemTest extends AbstractIntegrationTest {

    @Test
    void eagerManyToOne_causesSecondarySelectPerRow() {
        tx.executeWithoutResult(s -> {
            for (int i = 0; i < 3; i++) {
                EagerChild child = new EagerChild("eager-child-" + i);
                em.persist(child);
                em.persist(new EagerParent(child));
            }
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            List<EagerParent> parents = em.createQuery("select p from EagerParent p", EagerParent.class)
                    .getResultList();
            long after = sql.statementCount();

            assertEquals(3, parents.size());
            assertEquals(parents.size(), after - 1,
                    "EAGER issues one secondary select per row (N+1) even though we never touch the child");
        });
    }
}
