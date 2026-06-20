package com.example.jpatraining.catalog;

import com.example.jpatraining.common.Money;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Catalog product. Home of the unidirectional ManyToOne to {@link Brand} (FK {@code brand_id} on
 * product). Also has a ManyToOne to {@link Category} (bidirectional — {@code Category.products} is
 * the inverse). Reuses the {@link Money} embeddable for {@code price}.
 */
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @Embedded
    private Money price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    protected Product() {
    }

    public Product(String name, Money price, Brand brand, Category category) {
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public Brand getBrand() {
        return brand;
    }

    public Category getCategory() {
        return category;
    }
}
