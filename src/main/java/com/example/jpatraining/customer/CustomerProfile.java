package com.example.jpatraining.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;

/**
 * Shares its primary key with {@link Customer} via {@code @MapsId}: the profile's {@code id} <em>is</em>
 * the customer's id, and that single column is both the PK and the FK to {@code customers}. There is
 * no separate id generation — the id is derived from the associated Customer.
 *
 * <p>This is the owning side of the OneToOne (it holds the join column).
 */
@Entity
public class CustomerProfile {

    @Id
    private Long id; // populated from Customer via @MapsId

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Customer customer;

    private String bio;

    private LocalDate birthDate;

    protected CustomerProfile() {
    }

    public CustomerProfile(String bio, LocalDate birthDate) {
        this.bio = bio;
        this.birthDate = birthDate;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getBio() {
        return bio;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
}
