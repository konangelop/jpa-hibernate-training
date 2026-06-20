package com.example.jpatraining.onetoone;

import com.example.jpatraining.customer.Customer;
import com.example.jpatraining.customer.CustomerProfile;
import com.example.jpatraining.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Customer <-> CustomerProfile}: bidirectional OneToOne sharing a primary key via
 * {@code @MapsId}. The profile's PK column is ALSO the FK to {@code customers} — one column, both
 * roles. Because the key is shared, the customer reference is a proxy keyed by the already-known id.
 */
class OneToOneSharedPkTest extends AbstractIntegrationTest {

    @Test
    void sharedPrimaryKey_idIsFreeButColumnsCostOneSelect() {
        long[] ids = tx.execute(s -> {
            Customer customer = new Customer("mapsid@example.com", "Mara");
            CustomerProfile profile = new CustomerProfile("loves jpa", LocalDate.of(1990, 5, 1));
            customer.setProfile(profile);
            em.persist(customer); // cascade ALL persists the profile; @MapsId derives its id
            return new long[]{customer.getId(), profile.getId()};
        });

        assertEquals(ids[0], ids[1], "@MapsId means the profile's id IS the customer's id");

        tx.executeWithoutResult(s -> {
            sql.reset();
            CustomerProfile profile = em.find(CustomerProfile.class, ids[1]);
            long afterLoad = sql.statementCount();

            Long customerId = profile.getCustomer().getId(); // shared id is already known -> no select
            long afterReadId = sql.statementCount();

            String name = profile.getCustomer().getName(); // real column -> initializes the proxy
            long afterReadName = sql.statementCount();

            assertEquals(ids[0], customerId);
            assertEquals(0, afterReadId - afterLoad, "reading the shared id needs no select");
            assertEquals(1, afterReadName - afterReadId, "initializing the customer proxy is one select");
        });
    }
}
