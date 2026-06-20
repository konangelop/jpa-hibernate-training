package com.example.jpatraining.ordering;

import com.example.jpatraining.catalog.Product;
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
 * Child in the {@code Order <-> OrderItem} parent–child relationship and the owning side of it
 * (holds {@code order_id}). Also references the {@link Product} it represents (ManyToOne) and
 * carries link attributes — {@code quantity} and an embedded {@link Money} {@code unitPrice}.
 *
 * <p>This is the join <em>entity</em> through which Order and Product relate (a ManyToMany with extra
 * columns); that view is explored in the ManyToMany chapter.
 */
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    @Embedded
    private Money unitPrice;

    protected OrderItem() {
    }

    public OrderItem(Product product, int quantity, Money unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    // package-private: set by Order.addItem/removeItem so the owning side stays consistent.
    void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}
