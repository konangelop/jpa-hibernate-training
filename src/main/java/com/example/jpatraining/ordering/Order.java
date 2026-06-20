package com.example.jpatraining.ordering;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Central ordering entity. In this pass it is the home of two OneToOne mappings:
 * <ul>
 *   <li><strong>unidirectional</strong> OneToOne to {@link Shipment}: Order owns the FK
 *       ({@code shipment_id}) — see {@link #shipment};</li>
 *   <li><strong>bidirectional</strong> OneToOne with {@link Payment}: Payment owns the FK, Order is
 *       the inverse side ({@code mappedBy = "order"}) — see {@link #payment}.</li>
 * </ul>
 * It gains its customer and order items in later passes.
 */
@Entity
@Table(name = "orders") // "order" is a SQL reserved word
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    protected Order() {
    }

    public Order(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Payment getPayment() {
        return payment;
    }

    // Payment is the owning side; its constructor calls this to keep the inverse side in sync.
    void setPayment(Payment payment) {
        this.payment = payment;
    }
}
