package com.example.jpatraining.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Test infrastructure beans, imported by {@link AbstractIntegrationTest}.
 *
 * <p>The {@link PostgreSQLContainer} is exposed as a {@code @Bean} annotated with
 * {@link ServiceConnection}, so Spring Boot auto-wires the datasource to the container and
 * manages its lifecycle. Because every test shares this same configuration, Spring's
 * application-context cache reuses a single container across the whole suite.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // Testcontainers 2.0: PostgreSQLContainer moved to org.testcontainers.postgresql and is
        // no longer self-parameterized, so no <> here.
        return new PostgreSQLContainer("postgres:17");
    }

    /**
     * Spring Boot auto-configures a {@link PlatformTransactionManager} but not a
     * {@link TransactionTemplate}; tests use this for explicit transaction boundaries.
     */
    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
