package com.es.phoneshop.service.impl;

import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.RecentlyViewedProductsService;
import com.es.phoneshop.utils.ReadWriteLockWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class RecentlyViewedProductsServiceImpl implements RecentlyViewedProductsService {
    private static volatile RecentlyViewedProductsServiceImpl instance;
    private final ReadWriteLockWrapper readWriteLock;
    private static final String RECENTLY_VIEWED_PRODUCT_SESSION_ATTRIBUTE = RecentlyViewedProductsServiceImpl.class.getName();
    private static final int RECENTLY_VIEWED_PRODUCT_MAX_ALLOWED_AMOUNT = 3;

    private RecentlyViewedProductsServiceImpl() {
        this.readWriteLock = new ReadWriteLockWrapper();
    }

    public static RecentlyViewedProductsServiceImpl getInstance() {
        if (instance == null) {
            synchronized (RecentlyViewedProductsServiceImpl.class) {
                if (instance == null) {
                    instance = new RecentlyViewedProductsServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void addViewedProduct(HttpServletRequest request, Product product) {
        readWriteLock.write(() -> {
            HttpSession session = request.getSession();
            Deque<Product> products = getViewedProducts(request);
            products.addFirst(product);
            products = deleteDuplicateProducts(products);
            if (products.size() > RECENTLY_VIEWED_PRODUCT_MAX_ALLOWED_AMOUNT) {
                products.removeLast();
            }
            session.setAttribute(RECENTLY_VIEWED_PRODUCT_SESSION_ATTRIBUTE, products);
        });
    }

    private Deque<Product> deleteDuplicateProducts(Deque<Product> products) {
        return products.stream()
                .distinct()
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Override
    public Deque<Product> getViewedProducts(HttpServletRequest request) {
        return readWriteLock.writeWithReturn(() -> {
            HttpSession session = request.getSession();
            Deque<Product> products = (Deque<Product>) session.getAttribute(RECENTLY_VIEWED_PRODUCT_SESSION_ATTRIBUTE);
            if (products == null) {
                products = new LinkedList<>();
                session.setAttribute(RECENTLY_VIEWED_PRODUCT_SESSION_ATTRIBUTE, products);
            }
            return products;
        });
    }
}
