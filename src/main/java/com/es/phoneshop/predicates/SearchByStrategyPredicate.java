package com.es.phoneshop.predicates;

import com.es.phoneshop.enums.SearchMethod;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.utils.SearchStrategy;
import com.es.phoneshop.utils.impl.SearchStrategyFactory;

import java.util.function.Predicate;

public class SearchByStrategyPredicate implements Predicate<Product> {
    private final SearchStrategy searchStrategy;

    public SearchByStrategyPredicate(String query, SearchMethod searchMethod) {
        searchStrategy = SearchStrategyFactory.createStrategy(query, searchMethod);
    }

    @Override
    public boolean test(Product product) {
        return searchStrategy.search(product.getDescription());
    }
}
