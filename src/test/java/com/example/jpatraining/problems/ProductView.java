package com.example.jpatraining.problems;

import java.math.BigDecimal;

/**
 * Spring Data <strong>interface-based projection</strong> for {@code DtoProjectionProblemTest} — the
 * declarative counterpart to the {@link ProductSummary} constructor projection. A closed projection
 * (only mapped getters, including the nested embedded {@code price.amount}) makes Spring Data issue a
 * query selecting just those columns.
 */
public interface ProductView {

    String getName();

    PriceView getPrice();

    interface PriceView {
        BigDecimal getAmount();
    }
}
