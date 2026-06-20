package com.example.jpatraining.problems;

import java.math.BigDecimal;

/** DTO for a constructor projection in {@code DtoProjectionProblemTest} (not an entity). */
public record ProductSummary(String name, BigDecimal amount) {
}
