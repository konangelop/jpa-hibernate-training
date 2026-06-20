package com.example.jpatraining.customer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Customer aggregate root. In this pass it is the home of a bidirectional, <strong>shared primary
 * key</strong> OneToOne with {@link CustomerProfile} (the profile uses {@code @MapsId}). Customer is
 * the inverse side ({@code mappedBy = "customer"}); the profile owns the shared PK/FK column.
 *
 * <p>Gains its orders and addresses in later passes.
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CustomerProfile profile;

    protected Customer() {
    }

    public Customer(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public CustomerProfile getProfile() {
        return profile;
    }

    /** Owning-side-aware helper: links both directions so {@code @MapsId} can derive the profile's id. */
    public void setProfile(CustomerProfile profile) {
        if (profile != null) {
            profile.setCustomer(this);
        } else if (this.profile != null) {
            this.profile.setCustomer(null);
        }
        this.profile = profile;
    }
}
