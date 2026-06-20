package com.example.jpatraining.catalog;

import com.example.jpatraining.common.Money;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.util.HashSet;
import java.util.Set;

/**
 * Catalog product. Relationships:
 * <ul>
 *   <li>unidirectional ManyToOne to {@link Brand} (FK {@code brand_id});</li>
 *   <li>ManyToOne to {@link Category} (bidirectional — {@code Category.products} is the inverse);</li>
 *   <li><b>pure bidirectional ManyToMany to {@link Tag}</b> — Product is the OWNING side (declares the
 *       {@code @JoinTable product_tag}); {@code Tag.products} is {@code mappedBy}.</li>
 * </ul>
 * Reuses the {@link Money} embeddable for {@code price}. A {@code Set} is used for the ManyToMany
 * (the recommended collection type — see chapter 05).
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_tag",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

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

    public Set<Tag> getTags() {
        return tags;
    }

    /** Owning-side-aware helper: maintains both ends of the bidirectional ManyToMany. */
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getProducts().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getProducts().remove(this);
    }
}
