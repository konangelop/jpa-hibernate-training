package com.example.jpatraining.manytoone_onetomany;

import com.example.jpatraining.customer.Address;
import com.example.jpatraining.customer.Customer;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Customer -> Address}: unidirectional OneToMany mapped with {@code @JoinColumn}. The FK
 * ({@code customer_id}) is placed on the address table, so there is NO join table (see the DDL in
 * chapter 04). The collection is lazy and loads with a single select from the address table.
 */
class UnidirectionalOneToManyJoinColumnTest extends AbstractIntegrationTest {

    @Test
    void joinColumn_collectionIsLazyAndLoadsInOneSelect() {
        Long customerId = tx.execute(s -> {
            Customer c = new Customer("addr@example.com", "Andy");
            c.addAddress(new Address("1 Main St", "Town", "12345"));
            c.addAddress(new Address("2 Oak Ave", "Town", "12346"));
            em.persist(c); // cascade = ALL persists the addresses
            return c.getId();
        });

        tx.executeWithoutResult(s -> {
            sql.reset();
            Customer c = em.find(Customer.class, customerId);
            long afterLoad = sql.statementCount();
            int addressCount = c.getAddresses().size();
            long afterTouch = sql.statementCount();

            assertEquals(2, addressCount);
            assertEquals(1, afterTouch - afterLoad,
                    "addresses load with a single select from the address table (FK, no join table)");
        });
    }
}
