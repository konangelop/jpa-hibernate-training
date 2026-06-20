package com.example.jpatraining.ordering;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Target of the <strong>unidirectional</strong> OneToOne from {@link Order}. The foreign key lives
 * on {@code orders} (Order is the owning side); Shipment has no back-reference to Order — that is
 * exactly what makes the association unidirectional.
 */
@Entity
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String carrier;

    private String trackingNumber;

    protected Shipment() {
    }

    public Shipment(String carrier, String trackingNumber) {
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
    }

    public Long getId() {
        return id;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
