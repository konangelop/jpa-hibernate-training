package com.example.jpatraining.problems;

import com.example.jpatraining.ordering.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * <strong>Anti-pattern, test-only.</strong> Maps an enum with {@code @Enumerated(EnumType.ORDINAL)},
 * which stores the constant's <em>position</em> as an integer. Reordering or inserting constants then
 * silently remaps existing rows. {@code EnumMappingProblemTest} proves it stores an int; the real
 * model uses {@code STRING} everywhere.
 */
@Entity
public class OrdinalEnumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    private OrderStatus status;

    protected OrdinalEnumEntity() {
    }

    public OrdinalEnumEntity(OrderStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
