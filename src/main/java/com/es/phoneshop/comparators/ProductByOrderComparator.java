package com.es.phoneshop.comparators;

import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.model.product.Product;

import java.util.Comparator;

public class ProductByOrderComparator implements Comparator<Product> {
    private final SortField sortField;
    private final SortOrder sortOrder;

    public ProductByOrderComparator(SortField sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(Product product1, Product product2) {
        if (sortField == null || sortOrder == null)
            return 0;
        if (sortField == SortField.DESCRIPTION) {
            return sortByDescription(sortOrder, product1, product2);
        } else {
            return sortByPrice(sortOrder, product1, product2);
        }
    }

    private int sortByPrice(SortOrder sortOrder, Product product1, Product product2) {
        if (sortOrder == SortOrder.ASC) {
            return product1.getPrice().compareTo(product2.getPrice());
        }
        return product2.getPrice().compareTo(product1.getPrice());
    }

    private int sortByDescription(SortOrder sortOrder, Product product1, Product product2) {
        if (sortOrder == SortOrder.ASC) {
            return product1.getDescription().compareTo(product2.getDescription());
        }
        return product2.getDescription().compareTo(product1.getDescription());
    }
}
