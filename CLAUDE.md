# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

**Foundation complete (Pass 1).** The Maven project is scaffolded and the test harness is proven
end-to-end against the real stack. What exists today: `pom.xml`, `src/main/resources/application.yml`,
`docker-compose.yml`, `JpaTrainingApplication`, and a `src/test/.../support/` harness —
`AbstractIntegrationTest` (Testcontainers Postgres via `@ServiceConnection`, intentionally **not**
`@Transactional`), `SqlCounter` (Hibernate `Statistics` query-counter), `TestDataFactory`, and a
`HarnessSmokeTest` that **passes** (it boots the context against a real Postgres 17 container and
asserts the counter sees exactly one SQL statement).

**Pass 2 complete (embeddable + OneToOne).** The `Money` `@Embeddable` plus the OneToOne home —
`Order`→`Shipment` (unidirectional FK), `Order`↔`Payment` (bidirectional, owning = `Payment`),
`Customer`↔`CustomerProfile` (`@MapsId` shared PK) — with Spring Data repos, query-count tests
(`src/test/.../embeddables/`, `src/test/.../onetoone/`), and tutorial chapters `docs/02`, `docs/03`.
All six tests green. Entities use `SEQUENCE` id generation and all-LAZY associations.

The full design (the ~12-entity domain model, the common-problems catalog, the chapter list) lives
in `~/.claude/plans/shiny-watching-marble.md` — the source of truth for the remaining passes
(ManyToOne/OneToMany → ManyToMany → fetching → problems → docs). The
pass-by-pass breakdown is in `~/.claude/plans/ultraplan-ticklish-yao.md`. Update this file as each
pass lands real structure.

## What this project is

A **learning resource**, not a product. It teaches JPA / Hibernate entity relationships and
common pitfalls through one realistic **e-commerce** domain model. Two non-obvious consequences
shape every decision here:

- **Tests are the documentation.** Each concept and each "common problem" is a JUnit
  integration test, written as a **bad/good pair**: a "bad" test that reproduces the problem and
  a "good" test that shows the fix. Correctness is asserted by **counting the SQL Hibernate
  actually issues** (via Hibernate `Statistics`), so e.g. N+1 is a concrete failing-vs-passing
  number, not prose. When adding a concept, add its test pair; when changing a mapping, expect
  query-count assertions to move.
- **The markdown tutorial in `docs/` is a first-class deliverable.** Chapters are organized by
  concept and each claim links to the exact test that proves it. Code and docs must stay in sync.

## Stack

Java 21 (LTS) · Maven · Spring Boot **4.1.x** · Spring Framework 7 · Spring Data JPA ·
Jakarta Persistence **3.2** (spec) · Hibernate ORM **7.x** (provider) · PostgreSQL.
Tests use **Testcontainers** (real Postgres, not H2) so behavior matches the production dialect.

> These versions are newer than most training data. Verify exact property names / APIs
> (`@ServiceConnection`, `spring.jpa.*`, Hibernate `Statistics`, Jakarta Persistence 3.2
> specifics) against **Context7** docs rather than assuming.

> **Confirmed in Pass 1:** Spring Boot **4.1.0** GA · Hibernate ORM **7.4.1** · Testcontainers
> **2.0.5**. Two realities that differ from older docs/training, already baked into the code:
> 1. **Testcontainers 2.0 renamed its modules.** The Postgres dependency is
>    `org.testcontainers:testcontainers-postgresql` (not `:postgresql`), and the class is
>    `org.testcontainers.postgresql.PostgreSQLContainer` — **no longer self-generic**, so write
>    `PostgreSQLContainer` / `new PostgreSQLContainer("postgres:17")` with no `<>`.
> 2. **Hibernate 7 SQL bind-parameter logger is `org.hibernate.orm.jdbc.bind`** (not the old
>    `BasicBinder`). SQL statements still log under `org.hibernate.SQL`.
>
> The query-count primitive is `Statistics.getPrepareStatementCount()` (catches N+1;
> `getQueryExecutionCount()` does not). YAML keys with dots need bracket-quoting under
> `spring.jpa.properties`, e.g. `"[hibernate.generate_statistics]": true`.

## Commands

Once scaffolded (Maven wrapper expected at `./mvnw`):

```bash
# Run the full test suite (requires Docker running — Testcontainers starts Postgres)
./mvnw test

# Run a single test class / method
./mvnw test -Dtest=ClassName
./mvnw test -Dtest=ClassName#methodName

# Run the app against the docker-compose Postgres
docker compose up -d
./mvnw spring-boot:run

# Build
./mvnw clean package
```

Docker must be running for both the test suite (Testcontainers) and local app runs.

### Local build environment (this machine — TLS-scanning AV)

Norton "Web/Mail Shield" intercepts HTTPS here, which otherwise breaks Maven's downloads
(`PKIX path building failed` for the JVM; `CRYPT_E_NO_REVOCATION_CHECK` for the wrapper's curl).
Two **local-only, uncommitted** workarounds make `mvn` and `./mvnw` work:
- `.mvn/jvm.config` (gitignored) sets `-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT` so the Maven
  JVM trusts the Windows cert store (which holds Norton's root). Recreate it if it goes missing.
- The wrapper's Maven distribution is pre-seeded at `~/.m2/wrapper/dists/apache-maven-3.9.11/<hash>`
  (copied from the local Maven install) so `./mvnw` never runs its curl bootstrap download.
- **git** push/fetch over HTTPS otherwise fails with OpenSSL `unable to get local issuer
  certificate`; this repo sets `http.sslBackend=schannel` (repo-local `.git/config`, not committed)
  so git also uses the Windows cert store. No verification is disabled.

On a machine without TLS-scanning AV, none of this is needed — delete `.mvn/jvm.config`, unset the
git `sslBackend`, and downloads/pushes work normally.

## Architecture & conventions

**Package-by-concept** (not by layer). One coherent domain model lives under
`com.example.jpatraining.{catalog,customer,ordering}` with a shared `common` package
(e.g. the `Money` `@Embeddable`). Tests mirror the concepts: `onetoone/`,
`manytoone_onetomany/`, `manytomany/`, `fetching/`, `problems/`, plus a `support/` package for
the test harness (`AbstractIntegrationTest` with Testcontainers, the query-count helper, and a
`TestDataFactory`). There is intentionally **no REST/controller layer** — the focus is JPA,
exercised through tests.

**Each relationship type has one deliberate "home"** so concepts don't blur:

| Concept | Where it's demonstrated |
|---|---|
| OneToOne unidirectional (FK) | `Order` → `Shipment` |
| OneToOne bidirectional (FK) | `Order` ↔ `Payment` (owning = `Payment`) |
| OneToOne shared PK (`@MapsId`) | `Customer` ↔ `CustomerProfile` |
| ManyToOne unidirectional | `Product` → `Brand` |
| OneToMany unidirectional (`@JoinColumn`) | `Customer` → `Address` |
| OneToMany/ManyToOne bidirectional (the default) | `Customer` ↔ `Order` |
| Bidirectional + cascade + orphanRemoval | `Order` ↔ `OrderItem` |
| ManyToMany as a join entity w/ extra columns | `Order` ↔ `Product` via `OrderItem` |
| ManyToMany bidirectional, pure | `Product` ↔ `Tag` |
| ManyToMany unidirectional | `Customer` → wishlist `Set<Product>` |
| Self-referential | `Category` parent ↔ children |
| `@ElementCollection` | `Product.imageUrls` |

**Best-practice defaults to uphold when editing mappings** (these are what the project teaches):
all associations `LAZY` (override the spec's EAGER default on `@OneToOne`/`@ManyToOne`); use
`Set` for ManyToMany; model a join **entity** whenever a link carries attributes; manage
bidirectional links with owning-side helper methods (`addX`/`removeX`); keep
`spring.jpa.open-in-view=false`; fetch eagerly *per query* via `JOIN FETCH` / `@EntityGraph`
rather than via `FetchType.EAGER`.

When intentionally demonstrating an anti-pattern (e.g. EAGER pitfalls, unidirectional
`@OneToMany` join-table overhead, two-bag `MultipleBagFetchException`), keep it confined to its
`problems/` test and clearly label it — don't let anti-patterns leak into the core model.
