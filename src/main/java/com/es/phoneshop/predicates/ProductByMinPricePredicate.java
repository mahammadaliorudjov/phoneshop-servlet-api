package com.es.phoneshop.predicates;

import com.es.phoneshop.model.product.Product;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class ProductByMinPricePredicate implements Predicate<Product> {
    private final BigDecimal minPrice;

    public ProductByMinPricePredicate(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    @Override
    public boolean test(Product product) {
        return minPrice == null || product.getPrice().compareTo(minPrice) >= 0;
    }
}
