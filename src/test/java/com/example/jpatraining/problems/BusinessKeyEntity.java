package com.example.jpatraining.problems;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

/**
 * Test-only entity showing the recommended approach: {@code equals}/{@code hashCode} over a stable
 * <em>business key</em> ({@code code}), never the generated id. Membership in a {@code Set} survives
 * persistence.
 */
@Entity
public class BusinessKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String code;

    protected BusinessKeyEntity() {
    }

    public BusinessKeyEntity(String code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BusinessKeyEntity that)) {
            return false;
        }
        return Objects.equals(code, that.code); // stable across the entity's lifecycle
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
