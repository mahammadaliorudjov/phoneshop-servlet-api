package com.es.phoneshop.service.impl;

import com.es.phoneshop.model.product.Product;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecentlyViewedProductsServiceImplTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    private Product product1;
    private Product product2;
    private Product product3;
    private Product product4;
    private static final String SESSION_ATTRIBUTE = RecentlyViewedProductsServiceImpl.class.getName();
    private static final int MAX_ALLOWED = 3;
    private static final Long PRODUCT_ID_1 = 1L;
    private static final Long PRODUCT_ID_2 = 2L;
    private static final Long PRODUCT_ID_3 = 3L;
    private static final Long PRODUCT_ID_4 = 4L;
    private static final String DESCRIPTION_1 = "Product 1";
    private static final String DESCRIPTION_2 = "Product 2";
    private static final String DESCRIPTION_3 = "Product 3";
    private static final String DESCRIPTION_4 = "Product 4";
    private static final int RECENTLY_VIEWED_PRODUCTS_SIZE = 1;
    private RecentlyViewedProductsServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = RecentlyViewedProductsServiceImpl.getInstance();
        when(request.getSession()).thenReturn(session);

        product1 = new Product();
        product1.setId(PRODUCT_ID_1);
        product1.setDescription(DESCRIPTION_1);

        product2 = new Product();
        product2.setId(PRODUCT_ID_2);
        product2.setDescription(DESCRIPTION_2);

        product3 = new Product();
        product3.setId(PRODUCT_ID_3);
        product3.setDescription(DESCRIPTION_3);

        product4 = new Product();
        product4.setId(PRODUCT_ID_4);
        product4.setDescription(DESCRIPTION_4);
    }

    @Test
    public void testGetViewedProductsCreatesNewIfAbsent() {
        when(session.getAttribute(SESSION_ATTRIBUTE)).thenReturn(null);

        Deque<Product> viewedProducts = service.getViewedProducts(request);

        assertNotNull(viewedProducts);
        verify(session).setAttribute(eq(SESSION_ATTRIBUTE), any(Deque.class));
    }

    @Test
    public void testGetViewedProductsReturnsExisting() {
        Deque<Product> existingDeque = new LinkedList<>();
        existingDeque.add(product1);
        when(session.getAttribute(SESSION_ATTRIBUTE)).thenReturn(existingDeque);

        Deque<Product> result = service.getViewedProducts(request);

        assertSame(existingDeque, result);
    }

    @Test
    public void testAddViewedProductAddsProductToFront() {
        when(session.getAttribute(SESSION_ATTRIBUTE)).thenReturn(null);
        service.addViewedProduct(request, product1);
        verify(session, times(2)).setAttribute(eq(SESSION_ATTRIBUTE), any(Deque.class));

        Deque<Product> deque = new LinkedList<>();
        deque.add(product1);
        when(session.getAttribute(SESSION_ATTRIBUTE)).thenReturn(deque);

        Deque<Product> result = service.getViewedProducts(request);
        assertEquals(RECENTLY_VIEWED_PRODUCTS_SIZE, result.size());
        assertEquals(product1, result.peekFirst());
    }

    @Test
    public void testAddViewedProductRemovesDuplicate() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        doAnswer(invocation -> {
            String attributeName = (String) invocation.getArguments()[0];
            return sessionAttributes.get(attributeName);
        }).when(session).getAttribute(anyString());
        doAnswer(invocation -> {
            String attributeName = (String) invocation.getArguments()[0];
            Object value = invocation.getArguments()[1];
            sessionAttributes.put(attributeName, value);
            return null;
        }).when(session).setAttribute(anyString(), any());

        Deque<Product> deque = new LinkedList<>();
        deque.add(product1);
        sessionAttributes.put(SESSION_ATTRIBUTE, deque);

        service.addViewedProduct(request, product1);

        Deque<Product> result = service.getViewedProducts(request);
        assertEquals(1, result.size());
        assertEquals(product1, result.peekFirst());
    }

    @Test
    public void testAddViewedProductKeepsMaxAllowed() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        doAnswer(invocation -> {
            String attributeName = (String) invocation.getArguments()[0];
            return sessionAttributes.get(attributeName);
        }).when(session).getAttribute(anyString());

        doAnswer(invocation -> {
            String attributeName = (String) invocation.getArguments()[0];
            Object value = invocation.getArguments()[1];
            sessionAttributes.put(attributeName, value);
            return null;
        }).when(session).setAttribute(anyString(), any());

        service.addViewedProduct(request, product1);
        service.addViewedProduct(request, product2);
        service.addViewedProduct(request, product3);
        service.addViewedProduct(request, product4);

        Deque<Product> result = service.getViewedProducts(request);
        assertEquals(MAX_ALLOWED, result.size());
        assertEquals(product4, result.peekFirst());
        assertFalse(result.contains(product1));
    }
}
