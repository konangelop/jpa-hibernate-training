package com.example.jpatraining.ordering;

import com.example.jpatraining.common.Money;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

/**
 * Owning side of the bidirectional OneToOne with {@link Order}: Payment holds the FK
 * ({@code order_id}). Also demonstrates an {@link Embedded} {@link Money} value object.
 *
 * <p>Because Payment owns the association, its constructor links both sides
 * ({@code order.setPayment(this)}) so the in-memory object graph stays consistent.
 */
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Embedded
    private Money amount;

    private String method;

    protected Payment() {
    }

    public Payment(Order order, Money amount, String method) {
        this.order = order;
        this.amount = amount;
        this.method = method;
        order.setPayment(this); // keep the inverse side in sync
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Money getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }
}
