package com.es.phoneshop.predicates;

import com.es.phoneshop.model.product.Product;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class ProductByMaxPricePredicate implements Predicate<Product> {
    private final BigDecimal maxPrice;

    public ProductByMaxPricePredicate(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    @Override
    public boolean test(Product product) {
        return maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0;
    }
}
