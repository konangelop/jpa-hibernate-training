package com.example.jpatraining.catalog;

import com.example.jpatraining.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * A product review. Child of {@link Product} (ManyToOne, owning) and written by a {@link Customer}
 * (ManyToOne). Used by the fetching-strategies chapter to compare JOIN FETCH, {@code @EntityGraph},
 * {@code @BatchSize}, and pagination.
 */
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private int rating;

    @Column(length = 1000)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Customer author;

    protected Review() {
    }

    public Review(Customer author, int rating, String comment) {
        this.author = author;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Product getProduct() {
        return product;
    }

    // package-private: set by Product.addReview so the owning side stays consistent.
    void setProduct(Product product) {
        this.product = product;
    }

    public Customer getAuthor() {
        return author;
    }
}
