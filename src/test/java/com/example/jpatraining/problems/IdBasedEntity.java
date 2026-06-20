package com.example.jpatraining.problems;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

/**
 * <strong>Anti-pattern, test-only.</strong> {@code equals}/{@code hashCode} based on the generated
 * id — which is null before persist and changes afterwards, breaking {@code Set} membership.
 */
@Entity
public class IdBasedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String label;

    protected IdBasedEntity() {
    }

    public IdBasedEntity(String label) {
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdBasedEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id); // BUG: id is null before persist, non-null after
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // BUG: changes when the id is assigned on persist
    }
}
