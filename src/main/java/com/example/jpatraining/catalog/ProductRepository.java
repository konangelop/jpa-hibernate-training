package com.example.jpatraining.catalog;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Loads the matching products with their {@code reviews} eagerly via an entity graph. */
    @EntityGraph(attributePaths = "reviews")
    List<Product> findByIdIn(Collection<Long> ids);
}
