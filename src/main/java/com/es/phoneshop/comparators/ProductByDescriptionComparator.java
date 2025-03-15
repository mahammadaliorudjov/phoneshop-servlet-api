package com.es.phoneshop.comparators;

import com.es.phoneshop.model.Product;
import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class ProductByDescriptionComparator implements Comparator<Product> {
    private final String query;
    private static final String REGEX = " ";

    public ProductByDescriptionComparator(String query) {
        this.query = query;
    }

    @Override
    public int compare(Product product1, Product product2) {
        if (StringUtils.isBlank(query)) {
            return 0;
        }
        String[] words = query.split(REGEX);
        long product1AmountOfMatches = countMatches(words, product1.getDescription());
        long product2AmountOfMatches = countMatches(words, product2.getDescription());
        if (product1AmountOfMatches == product2AmountOfMatches) {
            int a = countAllWords(product1.getDescription());
            int b = countAllWords(product2.getDescription());
            return Integer.compare(a, b);
        }
        return Long.compare(product2AmountOfMatches, product1AmountOfMatches);
    }

    private long countMatches(String[] words, String productDescription) {
        String descriptionLower = productDescription.toLowerCase(Locale.ROOT);
        return Arrays.stream(words)
                .filter(word -> descriptionLower
                        .contains(word.toLowerCase(Locale.ROOT)))
                .count();
    }

    private int countAllWords(String product) {
        return product.split(REGEX).length;
    }
}
