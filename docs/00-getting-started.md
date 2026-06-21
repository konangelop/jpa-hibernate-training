# 00 — Getting started

This project teaches JPA / Hibernate **entity relationships** and the **common problems** that bite
in production, through one realistic e-commerce domain model. Its guiding idea:

> **The tests are the documentation.** Every claim in these chapters links to a JUnit test that
> proves it by counting the SQL Hibernate actually issues (via Hibernate `Statistics`). "N+1" isn't
> prose — it's a failing-vs-passing number.

## Prerequisites

- **JDK 21** — the bundled `./mvnw` wrapper provides Maven, so you don't need a separate install.
- **Docker** running — the test suite starts a real PostgreSQL via Testcontainers (no H2), so
  behaviour matches the production dialect.

## Run the tests

```bash
./mvnw test
```

The suite boots a Postgres 17 container once and reuses it. Each test arranges a small object graph,
resets the SQL counter, runs the code under test, and asserts the exact statement count.

## Run the app

```bash
docker compose up -d        # start the local Postgres
./mvnw spring-boot:run      # boots the app; DataSeeder populates a sample graph
```

`DataSeeder` (a `CommandLineRunner`, active in every profile *except* `test`) inserts two brands, a
small category tree, two products with tags/reviews, a customer with a profile/addresses/wishlist,
and an order with items, a payment, and a shipment. Inspect it with `psql`:

```bash
docker compose exec postgres psql -U jpa -d jpatraining -c "\dt"
docker compose exec postgres psql -U jpa -d jpatraining -c "select * from orders;"
```

## Project layout (package-by-concept)

```
common/    Money (@Embeddable value object)
catalog/   Brand, Category, Product, Tag, Review (+ repositories)
customer/  Customer, CustomerProfile, Address (+ repositories)
ordering/  Order, OrderItem, Payment, Shipment (+ repositories)
seed/      DataSeeder
src/test/.../support/   AbstractIntegrationTest, SqlCounter, TestDataFactory, ContainerConfig
src/test/.../{onetoone,manytoone_onetomany,manytomany,fetching,problems}/   the proving tests
```

There is intentionally **no REST/controller layer** — the focus is JPA, exercised through tests.

## The tutorial

1. [01 — JPA fundamentals](01-jpa-fundamentals.md) — entities, the persistence context, lazy vs eager, and how the SQL-counting harness works
2. [02 — Embeddables & value objects](02-embeddables-and-value-objects.md)
3. [03 — One-to-One](03-one-to-one.md)
4. [04 — Many-to-One & One-to-Many](04-many-to-one-and-one-to-many.md)
5. [05 — Many-to-Many](05-many-to-many.md)
6. [06 — Fetching strategies](06-fetching-strategies.md)
7. [07 — Common problems](07-common-problems.md)
8. [08 — Cheat sheet](08-cheatsheet.md)
