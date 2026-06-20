package com.example.jpatraining.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Embeddable value object for a monetary amount.
 *
 * <p>A value object has no identity of its own: equality is by value (amount + currency), and its
 * fields map to columns <em>in the owning entity's table</em> — no separate table, no foreign key.
 * Used by {@code Payment.amount} (and, in later passes, {@code OrderItem.unitPrice} and
 * {@code Product.price}).
 *
 * <p>Note the JPA requirement: an {@code @Embeddable} needs a (protected) no-arg constructor.
 */
@Embeddable
public class Money {

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    protected Money() {
        // required by JPA
    }

    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money other)) {
            return false;
        }
        return Objects.equals(amount, other.amount) && Objects.equals(currency, other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
