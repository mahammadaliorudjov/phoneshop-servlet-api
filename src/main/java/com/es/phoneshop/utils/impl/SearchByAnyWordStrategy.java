package com.es.phoneshop.utils.impl;

import com.es.phoneshop.utils.SearchStrategy;
import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;

public class SearchByAnyWordStrategy implements SearchStrategy {
    private final String query;
    private static final String REGEX = " ";

    public SearchByAnyWordStrategy(String query) {
        this.query = query;
    }

    @Override
    public boolean search(String description) {
        return StringUtils.isBlank(query) || Arrays.stream(query.split(REGEX))
                .anyMatch(word -> description.toLowerCase().contains(word.toLowerCase()));
    }
}
