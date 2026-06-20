package com.example.jpatraining.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

/**
 * Inverse side of the pure bidirectional {@code Product <-> Tag} ManyToMany. The owning side is
 * {@link Product#getTags()} (it declares the {@code @JoinTable}); here we only {@code mappedBy} it.
 */
@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> products = new HashSet<>();

    protected Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Product> getProducts() {
        return products;
    }
}
