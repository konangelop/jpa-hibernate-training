package com.example.jpatraining.manytoone_onetomany;

import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Self-referential {@code Category}: the parent link (ManyToOne) and the children link (OneToMany)
 * point at the same entity. Both are lazy — the parent is a proxy, the children a lazy collection.
 */
class SelfReferentialTest extends AbstractIntegrationTest {

    @Test
    void parentIsLazyProxy_andChildrenAreLazyCollection() {
        long[] ids = tx.execute(s -> {
            Category parent = new Category("Electronics-SR");
            Category child = new Category("Phones-SR");
            parent.addChild(child);
            em.persist(parent);
            em.persist(child);
            return new long[]{parent.getId(), child.getId()};
        });

        // child -> parent (ManyToOne) is a lazy proxy
        tx.executeWithoutResult(s -> {
            sql.reset();
            Category child = em.find(Category.class, ids[1]);
            long afterLoad = sql.statementCount();
            String parentName = child.getParent().getName();
            long afterTouch = sql.statementCount();

            assertEquals(1, afterLoad);
            assertEquals("Electronics-SR", parentName);
            assertEquals(1, afterTouch - afterLoad, "parent proxy initialized with one select");
        });

        // parent -> children (OneToMany) is a lazy collection
        tx.executeWithoutResult(s -> {
            sql.reset();
            Category parent = em.find(Category.class, ids[0]);
            long afterLoad = sql.statementCount();
            int childCount = parent.getChildren().size();
            long afterIterate = sql.statementCount();

            assertEquals(1, childCount);
            assertEquals(1, afterIterate - afterLoad, "children collection loaded with one select");
        });
    }
}
