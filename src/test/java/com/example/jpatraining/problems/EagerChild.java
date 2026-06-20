package com.example.jpatraining.problems;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Test-only entity used by {@code EagerFetchingProblemTest} (target of an EAGER ManyToOne). */
@Entity
public class EagerChild {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    protected EagerChild() {
    }

    public EagerChild(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
