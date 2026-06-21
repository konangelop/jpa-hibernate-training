# JPA & Hibernate Training

A hands-on learning resource for **JPA / Hibernate entity relationships** and the **common problems**
that bite in production (N+1, `LazyInitializationException`, `MultipleBagFetchException`, cartesian
products, OSIV, pagination-with-fetch, …) — taught through one realistic **e-commerce** domain model.

Its defining idea: **the tests are the documentation.** Each concept and each pitfall is a JUnit
integration test written as a *bad/good pair*, and correctness is asserted by **counting the SQL
Hibernate actually issues** (via Hibernate `Statistics`). So "N+1" is a concrete failing-vs-passing
number, not prose. The markdown tutorial under [`docs/`](docs/) is a co-equal deliverable — every
claim links to the test that proves it.

## Tutorial

Start at [docs/00 — Getting started](docs/00-getting-started.md), then:

| # | Chapter | Covers |
|---|---|---|
| 01 | [JPA fundamentals](docs/01-jpa-fundamentals.md) | entities vs value objects, persistence context, the SQL-counting harness |
| 02 | [Embeddables & value objects](docs/02-embeddables-and-value-objects.md) | `@Embeddable` `Money` |
| 03 | [One-to-One](docs/03-one-to-one.md) | unidirectional / bidirectional / `@MapsId`, the inverse-lazy trap |
| 04 | [Many-to-One & One-to-Many](docs/04-many-to-one-and-one-to-many.md) | the default mapping, self-ref, cascade + orphanRemoval, **N+1** |
| 05 | [Many-to-Many](docs/05-many-to-many.md) | join entity vs pure `@ManyToMany`, `Set` vs `List` |
| 06 | [Fetching strategies](docs/06-fetching-strategies.md) | `JOIN FETCH`, `@EntityGraph`, `@BatchSize`, pagination |
| 07 | [Common problems](docs/07-common-problems.md) | the bad/good catalog |
| 08 | [Cheat sheet](docs/08-cheatsheet.md) | decision tables + best-practice defaults |

## Stack

Java 21 · Maven · Spring Boot 4.1 · Hibernate ORM 7 · Jakarta Persistence 3.2 · PostgreSQL.
Tests run against **Testcontainers** (real Postgres, not H2), so behaviour matches the production dialect.

## Prerequisites

- **JDK 21** (a `./mvnw` wrapper is included, so a separate Maven install isn't required).
- **Docker** running — the test suite starts a Postgres container via Testcontainers.

## Commands

```bash
# Run the full test suite (Docker must be running)
./mvnw test

# Run a single test class / method
./mvnw test -Dtest=ClassName
./mvnw test -Dtest=ClassName#methodName

# Build
./mvnw clean package

# Run the app against the docker-compose Postgres (DataSeeder loads a sample graph)
docker compose up -d
./mvnw spring-boot:run
```

## Troubleshooting

**TLS errors during a build** — `PKIX path building failed` or `CRYPT_E_NO_REVOCATION_CHECK`. A
TLS-scanning antivirus/proxy (e.g. Norton "Web/Mail Shield") is intercepting HTTPS with a certificate
authority the Maven JVM doesn't trust. On Windows this is handled locally — and **not committed** — by
`.mvn/jvm.config` containing `-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT`, which makes the Maven JVM
trust the Windows certificate store. See the "Local build environment" note in `CLAUDE.md` for details.
