# 02 — Embeddables & Value Objects

> Proven by [`MoneyEmbeddableTest`](../src/test/java/com/example/jpatraining/embeddables/MoneyEmbeddableTest.java).

## The concept

Some things in a domain have **no identity of their own** — a money amount, an address, a date
range. Two `Money` objects of `99.90 EUR` are interchangeable; there's no "which one" to ask about.
These are **value objects**, and JPA maps them with `@Embeddable`: their fields become **columns in
the owning entity's table**. No separate table, no primary key, no foreign key, no extra select.

This project's value object is [`Money`](../src/main/java/com/example/jpatraining/common/Money.java)
(amount + currency), embedded in `Payment.amount` (and, in later chapters, `OrderItem.unitPrice` and
`Product.price`).

## The mapping

```java
@Embeddable
public class Money {
    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "currency", length = 3)
    private String currency;

    protected Money() { }            // JPA requires a no-arg constructor
    public Money(BigDecimal amount, String currency) { ... }
    // equals() + hashCode() over BOTH fields — equality is by value
}
```

```java
@Entity
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Embedded
    private Money amount;          // <-- lives inside the payment row
    ...
}
```

## What Hibernate generates

The embeddable's columns land directly in the owner's table — there is **no** `money` table:

```sql
create table payment (
    amount numeric(19,2),     -- Money.amount
    currency varchar(3),      -- Money.currency
    id bigint not null,
    order_id bigint not null unique,
    method varchar(255),
    primary key (id)
)
```

## What the test proves

`embeddedMoney_roundTripsAndStaysInOwnerTable` loads a `Payment` and asserts:

- **exactly one `select`** (`sql.assertSelectCount(1)`) — the embedded columns are read with the
  owner row; there is no join and no second query, because there is no separate table;
- the value **round-trips by value**: `payment.getAmount()` equals `Money.of("99.90", "EUR")`.

## Best practices (what this project upholds)

- **Equality by value.** Implement `equals`/`hashCode` over *all* fields. This is the opposite of
  entities (whose equality is by identity) — see chapter 07's "equals/hashCode on entities" problem.
- **Treat value objects as immutable.** Expose getters, no setters; "change" by replacing the whole
  `Money`. It keeps shared instances safe.
- **Reuse the same embeddable in many entities.** If one entity needs *two* of the same embeddable
  (e.g. `billingAddress` + `shippingAddress`), disambiguate the columns with `@AttributeOverride`.
- **Mind `BigDecimal` scale in `equals`.** `new BigDecimal("99.9")` ≠ `new BigDecimal("99.90")`
  (scale differs). Persist and compare at a consistent scale (here the column is `scale = 2`).

## Next

[03 — One-to-One](03-one-to-one.md): three ways to map a 1:1 relationship, and why the inverse side
of a bidirectional `@OneToOne` ignores `LAZY`.
