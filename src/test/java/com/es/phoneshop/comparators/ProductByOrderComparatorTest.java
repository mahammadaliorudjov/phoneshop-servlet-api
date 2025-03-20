package com.es.phoneshop.comparators;

import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.model.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductByOrderComparatorTest {
    @Mock
    private Product product1;
    @Mock
    private Product product2;
    private static final int ZERO = 0;
    private static final BigDecimal PRODUCT1_PRICE = BigDecimal.valueOf(1000);
    private static final BigDecimal PRODUCT2_PRICE = BigDecimal.valueOf(500);
    private static final String PRODUCT1_DESCRIPTION = "Antelope";
    private static final String PRODUCT2_DESCRIPTION = "Zebra";

    @Test
    public void compareWhenSortFieldIsNullShouldReturnZero() {
        ProductByOrderComparator comparator = new ProductByOrderComparator(null, SortOrder.ASC);

        assertEquals(ZERO, comparator.compare(product1, product2));
    }

    @Test
    public void compareWhenSortOrderIsNullShouldReturnZero() {
        ProductByOrderComparator comparator = new ProductByOrderComparator(SortField.PRICE, null);

        assertEquals(ZERO, comparator.compare(product1, product2));
    }

    @Test
    public void comparePriceAscShouldReturnPositiveWhenProduct1IsMoreExpensive() {
        when(product1.getPrice()).thenReturn(PRODUCT1_PRICE);
        when(product2.getPrice()).thenReturn(PRODUCT2_PRICE);
        ProductByOrderComparator comparator = new ProductByOrderComparator(SortField.PRICE, SortOrder.ASC);

        assertTrue(comparator.compare(product1, product2) > ZERO);
    }

    @Test
    public void comparePriceDescShouldReturnNegativeWhenProduct1IsMoreExpensive() {
        when(product1.getPrice()).thenReturn(PRODUCT1_PRICE);
        when(product2.getPrice()).thenReturn(PRODUCT2_PRICE);
        ProductByOrderComparator comparator = new ProductByOrderComparator(SortField.PRICE, SortOrder.DESC);

        assertTrue(comparator.compare(product1, product2) < ZERO);
    }

    @Test
    public void compareDescriptionAscShouldReturnAlphabeticalOrder() {
        when(product1.getDescription()).thenReturn(PRODUCT1_DESCRIPTION);
        when(product2.getDescription()).thenReturn(PRODUCT2_DESCRIPTION);
        ProductByOrderComparator comparator = new ProductByOrderComparator(SortField.DESCRIPTION, SortOrder.ASC);

        assertTrue(comparator.compare(product1, product2) < ZERO);
    }

    @Test
    public void compareDescriptionDescShouldReturnReverseAlphabeticalOrder() {
        when(product1.getDescription()).thenReturn(PRODUCT2_DESCRIPTION);
        when(product2.getDescription()).thenReturn(PRODUCT1_DESCRIPTION);
        ProductByOrderComparator comparator = new ProductByOrderComparator(SortField.DESCRIPTION, SortOrder.DESC);

        assertTrue(comparator.compare(product1, product2) < ZERO);
    }

    @Test
    public void compareWhenPricesAreEqualShouldReturnZero() {
        when(product1.getPrice()).thenReturn(PRODUCT1_PRICE);
        when(product2.getPrice()).thenReturn(PRODUCT1_PRICE);
        ProductByOrderComparator comparator = new ProductByOrderComparator(SortField.PRICE, SortOrder.ASC);

        assertEquals(0, comparator.compare(product1, product2));
    }

    @Test
    public void compareWhenDescriptionsAreEqualShouldReturnZero() {
        when(product1.getDescription()).thenReturn(PRODUCT1_DESCRIPTION);
        when(product2.getDescription()).thenReturn(PRODUCT1_DESCRIPTION);
        ProductByOrderComparator comparator = new ProductByOrderComparator(SortField.DESCRIPTION, SortOrder.ASC);

        assertEquals(ZERO, comparator.compare(product1, product2));
    }
}
