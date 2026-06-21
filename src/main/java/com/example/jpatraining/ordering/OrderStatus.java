package com.example.jpatraining.ordering;

/**
 * Lifecycle of an {@link Order}. Mapped with {@code @Enumerated(EnumType.STRING)} so the column
 * stores the constant <em>name</em> — stable if these constants are ever reordered. (The
 * {@code ORDINAL} alternative stores the position and is fragile; see the common-problems chapter.)
 */
public enum OrderStatus {
    NEW,
    PAID,
    SHIPPED,
    CANCELLED
}
