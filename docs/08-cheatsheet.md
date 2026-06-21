# 08 — Cheat sheet

A decision guide and the project's best-practice defaults. Each row links to the chapter that proves
it.

## Which relationship mapping?

| Situation | Use | Home |
|---|---|---|
| A value with no identity (money, address) | `@Embeddable` | [02](02-embeddables-and-value-objects.md) |
| 1:1, this entity should hold the FK | `@OneToOne` + `@JoinColumn` (owning) | [03](03-one-to-one.md) |
| 1:1, both navigable | bidirectional `@OneToOne` (`mappedBy` on inverse) | [03](03-one-to-one.md) |
| 1:1 sharing a key/lifecycle | `@OneToOne` + `@MapsId` (shared PK) | [03](03-one-to-one.md) |
| Many children point at one parent | `@ManyToOne` (owning) + `@OneToMany(mappedBy)` | [04](04-many-to-one-and-one-to-many.md) |
| Parent owns children's lifecycle | add `cascade = ALL` + `orphanRemoval = true` | [04](04-many-to-one-and-one-to-many.md) |
| Unidirectional 1:N | `@OneToMany` + `@JoinColumn` (FK on child, no join table) | [04](04-many-to-one-and-one-to-many.md) |
| Self-referential tree | `@ManyToOne parent` + `@OneToMany children` | [04](04-many-to-one-and-one-to-many.md) |
| M:N, link has **no** extra columns | pure `@ManyToMany` + `@JoinTable`, `Set` | [05](05-many-to-many.md) |
| M:N, link **has** extra columns | a join **entity** (two `@ManyToOne`) | [05](05-many-to-many.md) |

## Which fetch strategy?

| Need | Use | Home |
|---|---|---|
| One query needs an association | `JOIN FETCH` (JPQL) | [06](06-fetching-strategies.md) |
| A reusable fetch plan on a repo method | `@EntityGraph` | [06](06-fetching-strategies.md) |
| Avoid N+1 broadly, stay lazy, paginate-friendly | `@BatchSize` / `hibernate.default_batch_fetch_size` | [06](06-fetching-strategies.md) |
| Page parents **and** load a collection | two-step: page ids, then fetch | [06](06-fetching-strategies.md) |

## Best-practice defaults this project upholds

- **All associations `LAZY`** — override the spec's EAGER default on `@ManyToOne`/`@OneToOne`.
- **Fetch eagerly *per query*** (`JOIN FETCH`/`@EntityGraph`), never via `FetchType.EAGER`.
- **`Set` for `@ManyToMany`**; model a join **entity** as soon as the link carries attributes.
- **One owning side** = the FK holder; manage bidirectional links with `addX`/`removeX` helpers.
- **`spring.jpa.open-in-view=false`** — fetch inside a clear transaction boundary.
- **`SEQUENCE` id generation** (enables JDBC insert batching, unlike `IDENTITY`).
- **`equals`/`hashCode`**: value objects by value; entities by a stable business key (never the
  generated id), or rely on identity within a session.
- **Batch settings on**: `hibernate.jdbc.batch_size`, `order_inserts`, `order_updates`.

## Smells → fixes (see [07](07-common-problems.md))

| Symptom | Cause | Fix |
|---|---|---|
| Many tiny selects in a loop | N+1 (lazy iteration) | `JOIN FETCH` / `@EntityGraph` / `@BatchSize` |
| `LazyInitializationException` | lazy access after the tx closed | fetch in the query, or a correct tx boundary, or a DTO |
| `MultipleBagFetchException` | fetching two `List`s at once | `Set`, or one collection per query |
| Surprise joins / N+1 you can't disable | `FetchType.EAGER` | make it `LAZY` |
| Update "lost" | mutated the inverse side only | set the owning side (helper method) |
| `Set` membership breaks after save | `hashCode` uses the generated id | use a business key |
| `HHH000104`, wrong page size | collection `JOIN FETCH` + `setMaxResults` | page ids first, then fetch |

## Back to the start

[00 — Getting started](00-getting-started.md) · [README](../README.md)
