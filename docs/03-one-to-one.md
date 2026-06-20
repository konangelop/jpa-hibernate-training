# 03 — One-to-One

> Proven by [`OneToOneUnidirectionalTest`](../src/test/java/com/example/jpatraining/onetoone/OneToOneUnidirectionalTest.java),
> [`OneToOneBidirectionalTest`](../src/test/java/com/example/jpatraining/onetoone/OneToOneBidirectionalTest.java),
> and [`OneToOneSharedPkTest`](../src/test/java/com/example/jpatraining/onetoone/OneToOneSharedPkTest.java).

A 1:1 relationship can be mapped three ways. This project gives each a distinct "home" so the
differences stay sharp:

```mermaid
erDiagram
    ORDERS ||--o| SHIPMENT : "FK orders.shipment_id (unidirectional)"
    ORDERS ||--|| PAYMENT  : "FK payment.order_id (bidirectional, Payment owns)"
    CUSTOMERS ||--|| CUSTOMER_PROFILE : "shared PK via @MapsId"

    ORDERS {
        bigint id PK
        bigint shipment_id FK "unique"
        varchar order_number
    }
    PAYMENT {
        bigint id PK
        bigint order_id FK "unique"
        numeric amount
        varchar currency
    }
    CUSTOMER_PROFILE {
        bigint id PK_FK "= customers.id"
        varchar bio
        date birth_date
    }
```

The golden rule: **the owning side is whichever entity holds the foreign-key column.** The other
side is the *inverse* side and declares `mappedBy`.

---

## 1. Unidirectional (FK on the owner) — `Order → Shipment`

[`Order`](../src/main/java/com/example/jpatraining/ordering/Order.java) holds the FK; `Shipment` has
no back-reference.

```java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "shipment_id")
private Shipment shipment;
```

```sql
create table orders (
    id bigint not null,
    shipment_id bigint unique,          -- FK, unique => 1:1
    order_number varchar(255) not null unique,
    primary key (id)
)
```

**LAZY works on the owning side.** `owningSideLazy_shipmentLoadedOnlyWhenTouched` loads an `Order`,
then touches `order.getShipment().getCarrier()` and asserts that **touching the proxy adds exactly
one select** — i.e. the shipment was *not* loaded with the order.

---

## 2. Bidirectional (FK on one side) — `Order ↔ Payment`

[`Payment`](../src/main/java/com/example/jpatraining/ordering/Payment.java) is the **owning** side
(holds `order_id`); [`Order`](../src/main/java/com/example/jpatraining/ordering/Order.java) is the
**inverse** side.

```java
// Payment (owning): holds the FK
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "order_id", nullable = false, unique = true)
private Order order;

// Order (inverse): no column, just mappedBy
@OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
private Payment payment;
```

Keep both sides in sync with an owning-side helper — here `Payment`'s constructor calls
`order.setPayment(this)`.

### The lazy behaviour differs by side — and this is the famous trap

| Load | Touch the association | Extra selects | Proven by |
|---|---|---|---|
| `find(Payment)` | `payment.getOrder()` | **+1** (lazy proxy works) | `owningSide_orderIsLazyProxyUntilTouched` |
| `find(Order)` | `order.getPayment()` | **+0** (already loaded!) | `inverseSide_paymentResolvedDuringLoadDespiteLazy` |

**Why the inverse side ignores `LAZY`:** the inverse `Order.payment` is nullable and `Order` does
*not* hold the FK. To return a proxy, Hibernate would have to commit to "not null" — but it can't
know that without looking. So it issues a select **during the `find`** to learn whether a payment
exists. The `LAZY` hint is effectively ignored. Touching `getPayment()` afterwards therefore costs
nothing — it was already resolved.

**How to avoid it:**
- Put the FK on the side you load most, so it's the owning side (a real proxy);
- or use a **shared primary key** (`@MapsId`, below) — presence is implied by the key;
- or don't map the inverse side at all and fetch it per query / as a DTO when you need it;
- (advanced) bytecode enhancement with lazy attributes.
`optional = false` on the inverse side is *not* a reliable fix on its own.

---

## 3. Shared primary key (`@MapsId`) — `Customer ↔ CustomerProfile`

The best fit when two entities truly share a lifecycle and a key.
[`CustomerProfile`](../src/main/java/com/example/jpatraining/customer/CustomerProfile.java)'s primary
key **is** the customer's id, and the same column is the FK.

```java
@Entity
public class CustomerProfile {
    @Id
    private Long id;                       // no generator — comes from the customer

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Customer customer;
    ...
}
```

```sql
create table customer_profile (
    id bigint not null,        -- PK *and* FK to customers.id
    bio varchar(255),
    birth_date date,
    primary key (id)
)
-- alter table customer_profile add foreign key (id) references customers
```

`sharedPrimaryKey_idIsFreeButColumnsCostOneSelect` loads a `CustomerProfile` and proves the payoff of
a shared key:

- `profile.getCustomer().getId()` → **+0 selects**: the customer's id is the profile's id, already
  in hand;
- `profile.getCustomer().getName()` → **+1 select**: reading a real column initializes the proxy.

---

## Best-practice summary

- **Always map `@OneToOne` as `LAZY`** (the spec defaults to EAGER — override it).
- **Owning side = the entity with the FK column.** Inverse side uses `mappedBy` and an owning-side
  helper to stay in sync.
- **Prefer `@MapsId`** for same-lifecycle 1:1 (e.g. entity ↔ profile/detail): one fewer column, no
  nullable FK, and no inverse-side lazy trap.
- **Remember the inverse-side limitation:** a bidirectional `@OneToOne`'s inverse side is resolved
  eagerly on load. Design around it rather than fighting it.

## Next

[04 — Many-to-One & One-to-Many](04-many-to-one-and-one-to-many.md) — the bread-and-butter
relationship, and where N+1 first appears.
