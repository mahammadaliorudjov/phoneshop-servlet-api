package com.es.phoneshop.dao;

import com.es.phoneshop.enums.SearchMethod;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.model.product.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductDao {
    Product get(Long id);
    List<Product> findProducts(String query, SortField sortField, SortOrder sortOrder);
    List<Product> advancedSearch(String query, BigDecimal minPrice, BigDecimal maxPrice, SearchMethod searchMethod);
    void save(Product product);
    void delete(Long id);
}
