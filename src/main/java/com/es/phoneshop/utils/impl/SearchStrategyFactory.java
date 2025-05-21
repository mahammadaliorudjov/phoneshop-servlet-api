package com.es.phoneshop.utils.impl;

import com.es.phoneshop.enums.SearchMethod;
import com.es.phoneshop.utils.SearchStrategy;

public class SearchStrategyFactory {
    public static final String STRATEGY_NOT_FOUND_EXCEPTION = "Strategy does not exist";
    public static SearchStrategy createStrategy(String query, SearchMethod searchMethod) {
        if (searchMethod == SearchMethod.ANY_WORD) {
            return new SearchByAnyWordStrategy(query);
        } else if (searchMethod == SearchMethod.ALL_WORDS) {
            return new SearchByAllWordsStrategy(query);
        } else {
            throw new RuntimeException(STRATEGY_NOT_FOUND_EXCEPTION);
        }
    }
}
