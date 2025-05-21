package com.es.phoneshop.enums;

public enum SearchMethod {
    ANY_WORD("any word"), ALL_WORDS("all words");

    private final String searchType;

    SearchMethod(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchType() {
        return searchType;
    }
}
