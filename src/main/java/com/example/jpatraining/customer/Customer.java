package com.example.jpatraining.customer;

import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.ordering.Order;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Customer aggregate root. Relationships:
 * <ul>
 *   <li>OneToOne to {@link CustomerProfile} — bidirectional, shared PK via {@code @MapsId};</li>
 *   <li>OneToMany to {@link Order} — inverse of the bidirectional default ({@code mappedBy});</li>
 *   <li>unidirectional OneToMany to {@link Address} — {@code @JoinColumn}, no join table;</li>
 *   <li><b>unidirectional ManyToMany to {@link Product}</b> (wishlist) — {@code @JoinTable
 *       customer_wishlist}; Product has no back-reference.</li>
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "customer_wishlist",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> wishlist = new HashSet<>();

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

    public Set<Product> getWishlist() {
        return wishlist;
    }

    public void addToWishlist(Product product) {
        wishlist.add(product);
    }

    public void removeFromWishlist(Product product) {
        wishlist.remove(product);
    }
}
