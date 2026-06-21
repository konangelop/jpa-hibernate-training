package com.example.jpatraining.ordering;

import com.example.jpatraining.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * Central ordering entity, home of several relationships:
 * <ul>
 *   <li><b>ManyToOne to {@link Customer}</b> — the owning side of the bidirectional
 *       {@code Customer <-> Order} default mapping (FK {@code customer_id} on orders).</li>
 *   <li><b>OneToMany to {@link OrderItem}</b> — bidirectional parent–child with
 *       {@code cascade = ALL} and {@code orphanRemoval = true}; {@code OrderItem} is the owning side.</li>
 *   <li><b>unidirectional OneToOne to {@link Shipment}</b> (FK on orders);</li>
 *   <li><b>inverse OneToOne to {@link Payment}</b> ({@code mappedBy}; Payment owns the FK).</li>
 * </ul>
 * It also carries an {@code @Enumerated(STRING)} {@link OrderStatus} and an {@code @Version} field
 * for optimistic locking (see the common-problems chapter).
 */
@Entity
@Table(name = "orders") // "order" is a SQL reserved word
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /** Optimistic-lock version, managed by Hibernate (incremented on each update). */
    public long getVersion() {
        return version;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public List<OrderItem> getItems() {
        return items;
    }

    /** Owning-side-aware helper: links the item back to this order (sets its {@code order_id}). */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /** Removing from the collection triggers orphanRemoval — the item row is deleted on flush. */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
