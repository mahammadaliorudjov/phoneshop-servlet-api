package com.es.phoneshop.utils.impl;

import com.es.phoneshop.utils.SearchStrategy;
import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;

public class SearchByAllWordsStrategy implements SearchStrategy {
    private final String query;
    private static final String REGEX = " ";

    public SearchByAllWordsStrategy(String query) {
        this.query = query;
    }

    @Override
    public boolean search(String description) {
        return StringUtils.isBlank(query) || Arrays.stream(query.split(REGEX))
                .allMatch(word -> description.toLowerCase().contains(word.toLowerCase()));
    }
}
