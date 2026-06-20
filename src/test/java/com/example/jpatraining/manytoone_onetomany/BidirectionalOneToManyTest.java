package com.example.jpatraining.manytoone_onetomany;

import com.example.jpatraining.customer.Customer;
import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Customer <-> Order}: the recommended bidirectional default. The OWNING side is
 * {@code Order.customer} (FK {@code customer_id} on orders); {@code Customer.orders} is the inverse
 * ({@code mappedBy}). This is also where <strong>N+1</strong> first appears.
 *
 * <p>The N+1 test counts <em>collection loads</em> ({@code getCollectionLoadCount}) rather than raw
 * statements, to isolate the orders N+1 from unrelated entity-load noise — each loaded {@code Order}
 * also eager-resolves its inverse OneToOne {@code payment} (the chapter-03 gotcha), which would
 * otherwise inflate a raw statement count.
 */
class BidirectionalOneToManyTest extends AbstractIntegrationTest {

    @Test
    void owningSide_orderHoldsTheForeignKey() {
        long[] ids = tx.execute(s -> {
            Customer c = new Customer("bidi-fk@example.com", "Bob");
            em.persist(c);
            Order o = new Order("ORD-BIDI-FK");
            c.addOrder(o); // sets o.customer = c
            em.persist(o);
            return new long[]{c.getId(), o.getId()};
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Order o = em.find(Order.class, ids[1]);
            long afterLoad = sql.statementCount();

            Long customerId = o.getCustomer().getId(); // FK is in hand -> id is free
            long afterId = sql.statementCount();

            assertEquals(ids[0], customerId);
            assertEquals(0, afterId - afterLoad,
                    "owning side holds the FK, so the customer id is available without a select");
            assertEquals("Bob", o.getCustomer().getName()); // initializing the proxy works
        });
    }

    @Test
    void nPlusOne_iteratingOrdersPerCustomer_isFixedWithJoinFetch() {
        tx.executeWithoutResult(s -> {
            for (int i = 0; i < 3; i++) {
                Customer c = new Customer("nplus1-" + i + "@example.com", "C" + i);
                em.persist(c);
                for (int j = 0; j < 2; j++) {
                    Order o = new Order("NP1-" + i + "-" + j);
                    c.addOrder(o);
                    em.persist(o);
                }
            }
        });

        // BAD — one query for the customers, then one MORE orders-collection query per customer.
        tx.executeWithoutResult(s -> {
            sql.reset();
            List<Customer> customers = em.createQuery(
                    "select c from Customer c where c.email like 'nplus1-%'", Customer.class).getResultList();
            long collectionsAfterQuery = sql.collectionLoadCount();
            for (Customer c : customers) {
                c.getOrders().size(); // lazy collection -> a query per customer
            }
            long collectionsAfterIterate = sql.collectionLoadCount();

            assertEquals(3, customers.size());
            assertEquals(customers.size(), collectionsAfterIterate - collectionsAfterQuery,
                    "N+1: one orders-collection query per customer");
        });

        // GOOD — a single JOIN FETCH loads the customers and all their orders together.
        tx.executeWithoutResult(s -> {
            sql.reset();
            List<Customer> customers = em.createQuery(
                    "select distinct c from Customer c left join fetch c.orders where c.email like 'nplus1-%'",
                    Customer.class).getResultList();
            long collectionsAfterQuery = sql.collectionLoadCount();
            for (Customer c : customers) {
                c.getOrders().size();
            }
            long collectionsAfterIterate = sql.collectionLoadCount();

            assertEquals(3, customers.size());
            assertEquals(0, collectionsAfterIterate - collectionsAfterQuery,
                    "orders fetched in the query: no extra collection loads");
        });
    }
}
