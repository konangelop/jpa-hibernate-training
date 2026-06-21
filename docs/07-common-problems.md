# 07 — Common problems

The catalog. Each entry is a real failing-vs-passing behavior, proven by a test. Problems that were
demonstrated in earlier chapters are cross-linked rather than repeated.

| # | Problem | Proven by |
|---|---|---|
| 1 | N+1 selects | [ch.04 `BidirectionalOneToManyTest`](../src/test/java/com/example/jpatraining/manytoone_onetomany/BidirectionalOneToManyTest.java), [ch.06 `FetchingStrategiesTest`](../src/test/java/com/example/jpatraining/fetching/FetchingStrategiesTest.java) |
| 2 | `LazyInitializationException` / OSIV | [`LazyInitializationProblemTest`](../src/test/java/com/example/jpatraining/problems/LazyInitializationProblemTest.java) |
| 3 | `MultipleBagFetchException` | [`MultipleCollectionFetchProblemTest`](../src/test/java/com/example/jpatraining/problems/MultipleCollectionFetchProblemTest.java) |
| 4 | Cartesian product | [`MultipleCollectionFetchProblemTest`](../src/test/java/com/example/jpatraining/problems/MultipleCollectionFetchProblemTest.java) |
| 5 | EAGER pitfall | [`EagerFetchingProblemTest`](../src/test/java/com/example/jpatraining/problems/EagerFetchingProblemTest.java) |
| 6 | Unidirectional `@OneToMany` join table | [ch.04 `UnidirectionalOneToManyJoinColumnTest`](../src/test/java/com/example/jpatraining/manytoone_onetomany/UnidirectionalOneToManyJoinColumnTest.java) |
| 7 | Wrong owning side | [`WrongOwningSideProblemTest`](../src/test/java/com/example/jpatraining/problems/WrongOwningSideProblemTest.java) |
| 8 | `equals`/`hashCode` in a `Set` | [`EqualsHashCodeProblemTest`](../src/test/java/com/example/jpatraining/problems/EqualsHashCodeProblemTest.java) |
| 9 | Cascade misuse (transient) | [`CascadeProblemTest`](../src/test/java/com/example/jpatraining/problems/CascadeProblemTest.java) |
| 10 | `orphanRemoval` vs `CascadeType.REMOVE` | [ch.04 `CascadeOrphanRemovalTest`](../src/test/java/com/example/jpatraining/manytoone_onetomany/CascadeOrphanRemovalTest.java) |
| 11 | Open Session In View | this chapter + #2 |
| 12 | DTO / projection | [`DtoProjectionProblemTest`](../src/test/java/com/example/jpatraining/problems/DtoProjectionProblemTest.java) |
| 13 | Pagination + `JOIN FETCH` | [ch.06 `FetchingStrategiesTest`](../src/test/java/com/example/jpatraining/fetching/FetchingStrategiesTest.java) |
| 14 | Batch inserts | [`BatchInsertProblemTest`](../src/test/java/com/example/jpatraining/problems/BatchInsertProblemTest.java) |
| 15 | Optimistic locking (lost update) | [`OptimisticLockingProblemTest`](../src/test/java/com/example/jpatraining/problems/OptimisticLockingProblemTest.java) |
| 16 | `@Enumerated(ORDINAL)` fragility | [`EnumMappingProblemTest`](../src/test/java/com/example/jpatraining/problems/EnumMappingProblemTest.java) |

---

### 1 · N+1 selects
Iterating a lazy association over N parents fires one query per parent. **Fix:** `JOIN FETCH`,
`@EntityGraph`, or `@BatchSize` — per query, never EAGER. See chapters 04 and 06.

### 2 · LazyInitializationException
Touching a lazy association after the persistence context closed throws. **Fix:** fetch what you need
inside the transaction (`JOIN FETCH`/`@EntityGraph`) or return a DTO. Proven: accessing
`order.getItems()` after the tx fails; initializing it inside the tx succeeds.

### 3 · MultipleBagFetchException
Fetching two `List` (bag) collections in one query throws (`Category.children` + `products`).
**Fix:** use `Set`, fetch each collection in a **separate query**, or `@BatchSize`. Proven both ways.

### 4 · Cartesian product
Fetching two collections at once makes the DB return M×N rows. In Hibernate 6+ the **root entities are
auto-deduplicated**, so the old duplicate-rows correctness bug is gone — but the wasted M×N fetch is a
performance trap. **Fix:** one collection per query, or `@BatchSize`.

### 5 · EAGER pitfall
`@ManyToOne`/`@OneToOne` default to EAGER. EAGER doesn't help queries: a JPQL `select` issues a
**secondary select per row** (N+1) you can't disable at the call site. Proven: `select p from
EagerParent p` over 3 rows issues 3 extra child selects. **Fix:** map everything LAZY (as this project
does) and fetch per query.

### 6 · Unidirectional `@OneToMany` join table
A unidirectional `@OneToMany` **without** `@JoinColumn` creates a join table (and extra UPDATEs).
**Fix:** add `@JoinColumn` (FK on the child) or make it bidirectional. See `Customer → Address` (ch.04).

### 7 · Wrong owning side
Only the owning side (`Order.customer`) controls the FK; mutating the inverse collection alone writes
nothing. Proven: adding to `customer.getOrders()` without the helper leaves `customer_id` null.
**Fix:** owning-side-aware helpers (`addOrder`).

### 8 · equals/hashCode in a Set
Basing `equals`/`hashCode` on the generated id breaks `Set` membership when the id is assigned on
persist. Proven: an id-based element is lost from a `HashSet` after persist; a business-key element is
not. **Fix:** a stable business key (or leave default identity equality and avoid id-based hashing).

### 9 · Cascade misuse
Persisting a parent that points at a transient child with no cascade throws a transient-object error
(`Order → Shipment`). **Fix:** persist the child first, or add a cascade if the child belongs to the
parent (`Order → OrderItem`). The opposite mistake — `CascadeType.REMOVE`/`ALL` where children are
shared — deletes too much; cascade only true parent–child.

### 10 · orphanRemoval vs CascadeType.REMOVE
`CascadeType.REMOVE` deletes children when the **parent** is removed. `orphanRemoval = true` *also*
deletes a child the moment it's **removed from the collection** (disassociated). Proven in ch.04
(`removeItem` issues one delete). Use `orphanRemoval` for owned children.

### 11 · Open Session In View (OSIV)
Spring's `spring.jpa.open-in-view` defaults to **true**, holding the persistence context open for the
whole web request — which hides #2 by lazy-loading in the view layer (often as N+1). This project sets
it **false** (`application.yml`) so missing fetches fail fast in tests and you fetch deliberately.

### 12 · DTO / projection (over-fetching)
Loading whole entities when a screen needs a few fields wastes work and invites N+1. A constructor
projection (`select new ...Summary(p.name, p.price.amount)`) is a single query over just those columns.
Proven: one statement, no entities. Interface-based projections (Spring Data) do the same
declaratively — also proven here (`interfaceProjection_isASingleNarrowedQuery`).

### 13 · Pagination + JOIN FETCH (HHH000104)
A collection `JOIN FETCH` with `setMaxResults` paginates **in memory** (warning `HHH000104`). **Fix:**
page parent ids first, then fetch that page with its collection (or use `@BatchSize`). Proven in ch.06.

### 14 · Batch inserts
With `hibernate.jdbc.batch_size` + `order_inserts` (and SEQUENCE ids), many same-type inserts share one
batched statement. Proven: 20 inserts collapse to a handful of statements. IDENTITY ids would disable
batching (a round trip per row).

### 15 · Optimistic locking (lost update)
Two transactions read the same row; without protection the later commit silently overwrites the
earlier one (a "lost update"). An `@Version` column turns that into a loud failure. Proven: a stale
copy (version 0) re-saved after another transaction bumped the row to version 1 fails with an
optimistic-lock exception; a fresh load-modify-commit succeeds and increments the version. **Fix:**
add `@Version` to entities that can be edited concurrently.

### 16 · @Enumerated(ORDINAL) fragility
`@Enumerated(EnumType.ORDINAL)` stores an enum by its **position** (`PAID` → `1`). Reordering or
inserting constants then silently remaps every existing row. Proven via native SQL: the ORDINAL column
holds `1` while the STRING column holds `'PAID'`. **Fix:** always map enums with
`@Enumerated(EnumType.STRING)`.

## Next

[08 — Cheat sheet](08-cheatsheet.md): which mapping/strategy to reach for, and the project's
best-practice defaults in one place.
