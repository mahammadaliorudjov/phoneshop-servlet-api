package com.es.phoneshop.service;

import com.es.phoneshop.model.product.Product;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Deque;

public interface RecentlyViewedProductsService {
    void addViewedProduct(HttpServletRequest request, Product product);
    Deque<Product> getViewedProducts(HttpServletRequest request);
}
