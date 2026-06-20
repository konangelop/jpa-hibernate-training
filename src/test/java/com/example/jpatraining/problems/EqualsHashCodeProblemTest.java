package com.example.jpatraining.problems;

import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Problem #8 — {@code equals}/{@code hashCode} on entities placed in a {@code Set}. Basing them on
 * the generated id breaks set membership the moment the id is assigned; a stable business key fixes it.
 */
class EqualsHashCodeProblemTest extends AbstractIntegrationTest {

    @Test
    void idBasedHashCode_losesSetMembershipAfterPersist() {
        IdBasedEntity entity = new IdBasedEntity("x");
        Set<IdBasedEntity> set = new HashSet<>();
        set.add(entity); // id is still null here

        tx.executeWithoutResult(s -> em.persist(entity)); // id assigned -> hashCode changes

        assertFalse(set.contains(entity),
                "id-based hashCode: the element is lost from the Set once the id is generated");
    }

    @Test
    void businessKeyHashCode_keepsSetMembershipAfterPersist() {
        BusinessKeyEntity entity = new BusinessKeyEntity("CODE-BK");
        Set<BusinessKeyEntity> set = new HashSet<>();
        set.add(entity);

        tx.executeWithoutResult(s -> em.persist(entity));

        assertTrue(set.contains(entity), "business-key hashCode is stable across persist");
    }
}
