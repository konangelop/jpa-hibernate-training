# 01 — JPA fundamentals

A short on-ramp to the ideas every later chapter assumes. If you've used JPA before, skim to
"How this project proves things."

## Entity vs. value object

- An **entity** (`@Entity`) has an **identity** (a primary key) and a lifecycle; two entities are
  "the same" if they share an id, even if their fields differ. Examples here: `Customer`, `Order`,
  `Product`.
- A **value object** (`@Embeddable`) has **no identity** — it's defined entirely by its values, and
  its columns live in the owning entity's table. Example: [`Money`](02-embeddables-and-value-objects.md).

This distinction drives `equals`/`hashCode`: value objects compare by value; entities must *not* use
their generated id in `hashCode` (see [chapter 07](07-common-problems.md)).

## The persistence context

The `EntityManager` manages a **persistence context** — a first-level cache of managed entities,
scoped to a transaction in this project. Within it:

- entities are **managed**: changes are tracked and flushed (dirty checking);
- the same row is represented by **one** object instance (identity map);
- after the transaction ends, entities become **detached** — and touching an un-initialized lazy
  association then throws `LazyInitializationException`.

That last point is why the tests arrange data in one transaction and *act* in another (with the
context cleared in between) — otherwise the first-level cache would hide the very behaviour we're
measuring.

## Transactions and `open-in-view`

This project sets `spring.jpa.open-in-view=false`. OSIV (on by default in Spring Boot) keeps the
persistence context open for the whole web request, which silently papers over lazy-loading mistakes
and encourages N+1. Turning it off forces you to fetch what you need **inside** a clear transaction
boundary — the habit this project teaches.

## Lazy vs. eager, owning vs. inverse

- **Every association is mapped `LAZY`** here, overriding the spec's EAGER default on
  `@ManyToOne`/`@OneToOne`. Eager fetching is shown as an anti-pattern in chapter 07.
- The **owning side** is the one with the foreign-key column; the **inverse side** declares
  `mappedBy`. Only the owning side's state is written, so bidirectional links are kept consistent
  with owning-side helper methods (`addX`/`removeX`).

## How this project proves things — the SQL counter

Hibernate exposes runtime metrics through `org.hibernate.stat.Statistics` (enabled with
`hibernate.generate_statistics=true`). The test harness wraps it in
[`SqlCounter`](../src/test/java/com/example/jpatraining/support/SqlCounter.java):

```java
sql.reset();                       // statistics.clear()
... code under test ...
sql.assertSelectCount(1);          // statistics.getPrepareStatementCount() == 1
```

`getPrepareStatementCount()` counts **JDBC prepared statements** — the truest measure of "SQL that
hit the database," and the one that catches N+1 (each lazy load is another prepared statement).
`getQueryExecutionCount()` only counts HQL/criteria/native *query* executions and misses lazy loads,
so it's the wrong tool. The counter also exposes `entityLoadCount`, `collectionLoadCount`,
`insertCount`, `updateCount`, `deleteCount` for finer assertions.

Tests extend [`AbstractIntegrationTest`](../src/test/java/com/example/jpatraining/support/AbstractIntegrationTest.java)
(real Postgres via Testcontainers `@ServiceConnection`; deliberately **not** `@Transactional`) and
drive transactions explicitly via a `TransactionTemplate`.

## Field-level mappings worth knowing

Beyond relationships, two field mappings appear in the problems chapter: **`@Version`** (optimistic
locking — concurrent lost updates fail loudly instead of silently) and **`@Enumerated(EnumType.STRING)`**
(persist enums by name, never by `ORDINAL` position). See [chapter 07](07-common-problems.md) #15–#16.

## Next

[02 — Embeddables & value objects](02-embeddables-and-value-objects.md).
