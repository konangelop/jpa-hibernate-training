package com.example.jpatraining.seed;

import com.example.jpatraining.catalog.Brand;
import com.example.jpatraining.catalog.Category;
import com.example.jpatraining.catalog.Product;
import com.example.jpatraining.catalog.Review;
import com.example.jpatraining.catalog.Tag;
import com.example.jpatraining.common.Money;
import com.example.jpatraining.customer.Address;
import com.example.jpatraining.customer.Customer;
import com.example.jpatraining.customer.CustomerProfile;
import com.example.jpatraining.ordering.Order;
import com.example.jpatraining.ordering.OrderItem;
import com.example.jpatraining.ordering.OrderStatus;
import com.example.jpatraining.ordering.Payment;
import com.example.jpatraining.ordering.Shipment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Populates a small but complete sample graph for manual runs ({@code spring-boot:run}) so the schema
 * and data can be inspected via {@code psql}. Excluded from the {@code test} profile (which
 * {@link com.example.jpatraining.support.AbstractIntegrationTest} activates) so it never interferes
 * with the query-count tests.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        Brand acme = new Brand("Acme");
        Brand globex = new Brand("Globex");
        em.persist(acme);
        em.persist(globex);

        Category electronics = new Category("Electronics");
        Category phones = new Category("Phones");
        Category laptops = new Category("Laptops");
        electronics.addChild(phones);
        electronics.addChild(laptops);
        em.persist(electronics);
        em.persist(phones);
        em.persist(laptops);

        Tag fresh = new Tag("new");
        Tag popular = new Tag("popular");
        em.persist(fresh);
        em.persist(popular);

        Customer alice = new Customer("alice@example.com", "Alice");
        alice.setProfile(new CustomerProfile("Long-time customer", LocalDate.of(1990, 3, 14)));
        alice.addAddress(new Address("1 Main St", "Athens", "10001"));
        alice.addAddress(new Address("2 Beach Rd", "Athens", "10002"));
        em.persist(alice); // cascades profile + addresses

        Product phoneX = new Product("Phone X", Money.of("699.00", "EUR"), acme, phones);
        phoneX.addTag(fresh);
        phoneX.addTag(popular);
        phoneX.addImageUrl("https://img.example/phone-x-front.png");
        phoneX.addImageUrl("https://img.example/phone-x-back.png");
        phoneX.addReview(new Review(alice, 5, "Excellent phone"));
        Product laptopY = new Product("Laptop Y", Money.of("1299.00", "EUR"), globex, laptops);
        laptopY.addTag(popular);
        laptopY.addReview(new Review(alice, 4, "Solid laptop"));
        em.persist(phoneX); // cascades reviews
        em.persist(laptopY);

        alice.addToWishlist(laptopY);

        Shipment shipment = new Shipment("UPS", "1Z-SAMPLE");
        em.persist(shipment);
        Order order = new Order("ORD-1001");
        alice.addOrder(order);
        order.setShipment(shipment);
        order.addItem(new OrderItem(phoneX, 1, phoneX.getPrice()));
        order.addItem(new OrderItem(laptopY, 2, laptopY.getPrice()));
        order.setStatus(OrderStatus.PAID);
        em.persist(order); // cascades order items

        Payment payment = new Payment(order, Money.of("3297.00", "EUR"), "CARD"); // links the order
        em.persist(payment);

        log.info("Seeded sample data: 2 brands, a 3-node category tree, 2 products, "
                + "1 customer (profile + 2 addresses + wishlist), and 1 order (2 items, payment, shipment).");
    }
}
