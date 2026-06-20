# 06 — Fetching strategies

> Proven by [`FetchingStrategiesTest`](../src/test/java/com/example/jpatraining/fetching/FetchingStrategiesTest.java).

Everything in this project is mapped `LAZY`. That's the right default — but it means you must decide,
**per use case**, how to pull in what a given operation needs, without triggering N+1. This chapter
compares four tools using `Product.reviews`.

The shared setup: 3 products, 2 reviews each. Naively iterating `product.getReviews()` over the list
would be N+1 (one extra query per product — see [chapter 04](04-many-to-one-and-one-to-many.md)).

## 1. `JOIN FETCH` (JPQL) — one query, ad hoc

```java
em.createQuery("select distinct p from Product p left join fetch p.reviews where p.id in :ids", Product.class)
```

`joinFetch_loadsAllReviewsInOneQuery`: **1 query**, and iterating the reviews afterwards adds **0**.
Use `distinct` to collapse the duplicate parent rows a collection join produces. Best when the fetch
plan is specific to one query.

## 2. `@EntityGraph` (Spring Data) — one query, declarative

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "reviews")
    List<Product> findByIdIn(Collection<Long> ids);
}
```

`entityGraph_loadsReviewsWithoutNPlusOne`: the reviews come back fetched (iterating adds **0**
queries). Same effect as `JOIN FETCH`, but reusable and declarative — the repository method declares
what to fetch. Best when a fetch plan is reused across call sites.

## 3. `@BatchSize` — fewer round trips, still lazy

```java
@OneToMany(mappedBy = "product", ...)
@BatchSize(size = 10)
private List<Review> reviews = new ArrayList<>();
```

`batchSize_loadsManyCollectionsInOneBatchedQuery`: with 3 products loaded and their reviews accessed,
Hibernate batch-loads **all three collections in one** `select ... where product_id in (?,?,?)` — so
N+1 collapses to `ceil(N / size) + 1`. It keeps associations lazy (no cartesian products), which is
why it pairs well with pagination. Set globally with `hibernate.default_batch_fetch_size`, or
per-association with `@BatchSize`.

## 4. Pagination + fetch — the HHH000104 trap

Combining a **collection** `JOIN FETCH` with `setMaxResults`/`setFirstResult` does *not* paginate at
the SQL level: Hibernate fetches **all** matching rows and trims in memory, logging
`HHH000104: firstResult/maxResults specified with collection fetch; applying in memory`. On a large
table that's a silent performance cliff.

The fix is **two steps** — page the ids first (no collection join, so the database applies `LIMIT`),
then fetch that page's entities with their collection:

```java
// 1) page the IDs at the SQL level
List<Long> pageIds = em.createQuery("select p.id from Product p order by p.id", Long.class)
        .setMaxResults(2).getResultList();
// 2) fetch the page with reviews
em.createQuery("select distinct p from Product p left join fetch p.reviews where p.id in :pageIds", Product.class)
        .setParameter("pageIds", pageIds).getResultList();
```

`pagination_twoStep_pagesAtTheSqlLevelWithoutNPlusOne`: **2 queries**, exactly the requested page
size, and no N+1. (`@BatchSize` is the other good option — paginate the parents, let batch fetching
pull the collections.)

## Which to use

| Need | Reach for |
|---|---|
| One specific query needs an association | `JOIN FETCH` |
| A reusable fetch plan on a repository method | `@EntityGraph` |
| Avoid N+1 broadly while staying lazy; pagination-friendly | `@BatchSize` / `default_batch_fetch_size` |
| Page parents **and** load a collection | two-step (ids, then fetch) — never collection-fetch + `setMaxResults` |

**Never** fix N+1 by switching the mapping to `EAGER` — that just moves the problem everywhere and is
covered as an anti-pattern in [chapter 07](07-common-problems.md).

## Next

[07 — Common problems](07-common-problems.md): the catalog of bad/good pairs — N+1, EAGER traps,
`MultipleBagFetchException`, cartesian products, `LazyInitializationException`, cascade/orphan
mistakes, and more.
