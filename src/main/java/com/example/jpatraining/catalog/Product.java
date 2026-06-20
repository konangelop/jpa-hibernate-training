package com.example.jpatraining.catalog;

import com.example.jpatraining.common.Money;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Catalog product. Relationships:
 * <ul>
 *   <li>unidirectional ManyToOne to {@link Brand} (FK {@code brand_id});</li>
 *   <li>ManyToOne to {@link Category} (bidirectional — {@code Category.products} is the inverse);</li>
 *   <li>pure bidirectional ManyToMany to {@link Tag} — owning side ({@code @JoinTable product_tag});</li>
 *   <li>OneToMany to {@link Review} — parent–child, carries {@code @BatchSize} for the fetching chapter.</li>
 * </ul>
 * Reuses the {@link Money} embeddable for {@code price}; uses a {@code Set} for the ManyToMany.
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10) // batch-fetch lazy review collections across products (see chapter 06)
    private List<Review> reviews = new ArrayList<>();

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

    public List<Review> getReviews() {
        return reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setProduct(this);
    }
}
