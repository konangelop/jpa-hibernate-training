package com.example.jpatraining.customer;

import com.example.jpatraining.ordering.Order;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer aggregate root, home of several relationships:
 * <ul>
 *   <li><b>OneToOne to {@link CustomerProfile}</b> — bidirectional, shared PK via {@code @MapsId}
 *       (the profile owns the shared key);</li>
 *   <li><b>OneToMany to {@link Order}</b> — the inverse side of the bidirectional default
 *       ({@code mappedBy = "customer"}; {@code Order} owns the FK);</li>
 *   <li><b>unidirectional OneToMany to {@link Address}</b> — {@code @JoinColumn} puts the FK on the
 *       address table, so there is NO join table.</li>
 * </ul>
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

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id") // unidirectional: FK on the address table, NO join table
    private List<Address> addresses = new ArrayList<>();

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

    public List<Order> getOrders() {
        return orders;
    }

    /** Owning side is {@code Order.customer}; this helper sets the FK and keeps the collection in sync. */
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void addAddress(Address address) {
        addresses.add(address);
    }
}
