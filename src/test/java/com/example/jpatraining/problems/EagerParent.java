package com.example.jpatraining.problems;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * <strong>Anti-pattern, test-only.</strong> An {@code @ManyToOne(fetch = EAGER)} — the spec default,
 * which this project otherwise overrides everywhere. Confined here to demonstrate the EAGER pitfall
 * in {@code EagerFetchingProblemTest}; it must never leak into the core model.
 */
@Entity
public class EagerParent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "child_id")
    private EagerChild child;

    protected EagerParent() {
    }

    public EagerParent(EagerChild child) {
        this.child = child;
    }

    public Long getId() {
        return id;
    }

    public EagerChild getChild() {
        return child;
    }
}
