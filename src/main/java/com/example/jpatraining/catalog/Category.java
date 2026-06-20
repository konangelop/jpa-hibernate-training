package com.example.jpatraining.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * Self-referential category tree: each Category has an optional {@link #parent} and a list of
 * {@link #children} — the same entity on both ends of a ManyToOne/OneToMany.
 *
 * <p>It is also the inverse side of {@code Product -> Category}, giving Category <em>two</em> bag
 * (List) collections, {@link #children} and {@link #products}. That is deliberate: it sets up the
 * {@code MultipleBagFetchException} demonstration in the common-problems chapter.
 */
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    protected Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getParent() {
        return parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    /** Owning-side-aware helper: the child's {@code parent} FK is what actually persists the link. */
    public void addChild(Category child) {
        children.add(child);
        child.parent = this;
    }

    public List<Product> getProducts() {
        return products;
    }
}
