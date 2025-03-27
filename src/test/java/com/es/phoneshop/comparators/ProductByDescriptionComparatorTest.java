package com.es.phoneshop.comparators;

import com.es.phoneshop.model.product.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductByDescriptionComparatorTest {
    @Mock
    private Product product1;
    @Mock
    private Product product2;
    private ProductByDescriptionComparator comparator;
    private static final String APPLE_DESCRIPTION = "Apple iPhone";
    private static final String SAMSUNG_DESCRIPTION = "Samsung Galaxy";
    private static final String QUERY = "apple";
    private static final int ZERO = 0;

    @Test
    public void compareWhenQueryIsBlankShouldReturnZero() {
        comparator = new ProductByDescriptionComparator("");

        assertEquals(ZERO, comparator.compare(product1, product2));
    }

    @Test
    public void compareWhenFirstProductHasMoreMatchesShouldReturnNegative() {
        comparator = new ProductByDescriptionComparator(QUERY);
        when(product1.getDescription()).thenReturn(APPLE_DESCRIPTION);
        when(product2.getDescription()).thenReturn(SAMSUNG_DESCRIPTION);

        assertTrue(comparator.compare(product1, product2) < ZERO);
    }

    @Test
    public void compareWhenSameMatchesButFirstHasFewerWordsShouldReturnNegative() {
        comparator = new ProductByDescriptionComparator(QUERY);
        when(product1.getDescription()).thenReturn(QUERY);
        when(product2.getDescription()).thenReturn(APPLE_DESCRIPTION);

        assertTrue(comparator.compare(product1, product2) < ZERO);
    }
}
