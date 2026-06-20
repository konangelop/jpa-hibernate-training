# JPA & Hibernate Training

A hands-on learning resource for **JPA / Hibernate entity relationships** and the **common problems**
that bite in production (N+1, `LazyInitializationException`, `MultipleBagFetchException`, cartesian
products, OSIV, pagination-with-fetch, …) — taught through one realistic **e-commerce** domain model.

Its defining idea: **the tests are the documentation.** Each concept and each pitfall is a JUnit
integration test written as a *bad/good pair*, and correctness is asserted by **counting the SQL
Hibernate actually issues** (via Hibernate `Statistics`). So "N+1" is a concrete failing-vs-passing
number, not prose. A markdown tutorial under `docs/` (added concept-by-concept) is a co-equal
deliverable.

## Status

**Foundation complete.** The project is scaffolded and the test harness is proven end-to-end: a
`HarnessSmokeTest` boots Spring Boot against a real Postgres container (Testcontainers) and verifies
the Hibernate-backed query-counter. Domain entities, the concept/problem tests, and the tutorial
chapters are built in subsequent passes (design: `~/.claude/plans/shiny-watching-marble.md`).

## Stack

Java 21 · Maven · Spring Boot 4.1 · Hibernate ORM 7 · Jakarta Persistence 3.2 · PostgreSQL.
Tests run against **Testcontainers** (real Postgres, not H2), so behavior matches the production dialect.

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

# Run the app against the docker-compose Postgres
docker compose up -d
./mvnw spring-boot:run
```

## Troubleshooting

**TLS errors during a build** — `PKIX path building failed` or `CRYPT_E_NO_REVOCATION_CHECK`. A
TLS-scanning antivirus/proxy (e.g. Norton "Web/Mail Shield") is intercepting HTTPS with a certificate
authority the Maven JVM doesn't trust. On Windows this is handled locally — and **not committed** — by
`.mvn/jvm.config` containing `-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT`, which makes the Maven JVM
trust the Windows certificate store. See the "Local build environment" note in `CLAUDE.md` for details.
