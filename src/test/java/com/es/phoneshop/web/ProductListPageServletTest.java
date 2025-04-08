package com.es.phoneshop.web;

import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.exception.OutOfStockException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import com.es.phoneshop.service.impl.RecentlyViewedProductsServiceImpl;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ProductListPageServletTest {
    private static final String QUERY = "query";
    private static final String PRODUCT_DAO = "productDao";
    private static final String DESCRIPTION = "description";
    private static final String ASC = "asc";
    private static final String ERROR_MESSAGE = "Database error";
    private static final String ERROR = "error";
    private static final String QUANTITY = "quantity";
    private static final String INVALID_PRODUCT_ID = "asd";
    private static final String ERROR_PRODUCT_ID_ATTRIBUTE = "errorProductId";
    private static final String PRODUCT_ID = "productId";
    private static final String SERVLET_PATH = "/products";
    private static final String SUCCESS_MESSAGE = "?message=Cart updated successfully";
    private static final String QUANTITY_VALUE_STRING = "2";
    private static final String PRODUCT_ID_STRING = "1";
    private static final long PRODUCT_ID_LONG = 1;
    private static final int QUANTITY_VALUE_INT = 2;
    private static final String SESSION_ATTRIBUTE = RecentlyViewedProductsServiceImpl.class.getName();
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private ArrayListProductDao productDao;
    @Mock
    HttpSessionCartService cartService;
    @Mock
    Cart cart;
    private ProductListPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        servlet = new ProductListPageServlet();
        servlet.init(servletConfig);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(SESSION_ATTRIBUTE)).thenReturn(new LinkedList<>());
        when(cartService.getCart(request)).thenReturn(cart);
        when(request.getLocale()).thenReturn(Locale.US);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Test
    public void doPostValidQuantityRedirectsToProductsWithSuccess() throws Exception {
        try (MockedStatic<HttpSessionCartService> mocked = mockStatic(HttpSessionCartService.class)) {
            mocked.when(HttpSessionCartService::getInstance).thenReturn(cartService);

            ProductListPageServlet servlet = new ProductListPageServlet();
            servlet.init(servletConfig);

            when(request.getParameter(QUANTITY)).thenReturn(QUANTITY_VALUE_STRING);
            when(request.getParameter(PRODUCT_ID)).thenReturn(PRODUCT_ID_STRING);
            when(request.getContextPath()).thenReturn("");

            servlet.doPost(request, response);

            verify(cartService).add(cart, PRODUCT_ID_LONG, QUANTITY_VALUE_INT);
            verify(response).sendRedirect(SERVLET_PATH + SUCCESS_MESSAGE);
        }
    }

    @Test
    public void doPostInvalidQuantitySetsError() throws Exception {
        when(request.getParameter(QUANTITY)).thenReturn(QUANTITY);
        when(request.getParameter(PRODUCT_ID)).thenReturn(PRODUCT_ID_STRING);
        when(request.getLocale()).thenReturn(Locale.US);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq(ERROR), anyString());
        verify(request).setAttribute(eq(ERROR_PRODUCT_ID_ATTRIBUTE), eq(PRODUCT_ID_LONG));
    }

    @Test
    public void doPostOutOfStockExceptionSetsErrorAndForwards() throws Exception {
        try (MockedStatic<HttpSessionCartService> mocked = mockStatic(HttpSessionCartService.class)) {
            mocked.when(HttpSessionCartService::getInstance).thenReturn(cartService);

            ProductListPageServlet servlet = new ProductListPageServlet();
            servlet.init(servletConfig);

            when(request.getParameter(QUANTITY)).thenReturn(QUANTITY_VALUE_STRING);
            when(request.getParameter(PRODUCT_ID)).thenReturn(PRODUCT_ID_STRING);
            when(request.getLocale()).thenReturn(Locale.US);
            doThrow(new OutOfStockException("")).when(cartService).add(cart, PRODUCT_ID_LONG, QUANTITY_VALUE_INT);

            servlet.doPost(request, response);

            verify(request).setAttribute(eq(ERROR), anyString());
            verify(request).setAttribute(eq(QUANTITY), eq(QUANTITY_VALUE_STRING));
            verify(request).setAttribute(eq(ERROR_PRODUCT_ID_ATTRIBUTE), eq(PRODUCT_ID_LONG));
        }
    }

    @Test(expected = NumberFormatException.class)
    public void doPostInvalidProductIdThrowsNumberFormatException() throws Exception {
        when(request.getParameter(PRODUCT_ID)).thenReturn(INVALID_PRODUCT_ID);

        servlet.doPost(request, response);
    }

    @Test
    public void testDoGetSetsAttributesAndForwards() throws ServletException, IOException {
        Deque<Product> existingDeque = new LinkedList<>();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(SESSION_ATTRIBUTE)).thenReturn(existingDeque);

        servlet.doGet(request, response);

        verify(request, times(2)).setAttribute(anyString(), anyList());
        verify(requestDispatcher).forward(request, response);
    }

    @Test(expected = RuntimeException.class)
    public void testDoGetWhenDaoThrowsExceptionThrowsRuntimeException() throws ServletException, IOException {
        try (MockedStatic<ArrayListProductDao> mocked = mockStatic(ArrayListProductDao.class)) {
            mocked.when(ArrayListProductDao::getInstance).thenReturn(productDao);
            when(productDao.findProducts(QUERY,
                    SortField.valueOf(DESCRIPTION.toUpperCase()),
                    SortOrder.valueOf(ASC)))
                    .thenThrow(new RuntimeException(ERROR_MESSAGE));

            ProductListPageServlet servlet = new ProductListPageServlet();
            servlet.init(servletConfig);

            servlet.doGet(request, response);
        }
    }

    @Test
    public void testInitWhenProductDaoIsNullInitializesNewDao() throws Exception {
        ProductListPageServlet servlet = new ProductListPageServlet();
        servlet.init(servletConfig);

        Field field = ProductListPageServlet.class.getDeclaredField(PRODUCT_DAO);
        field.setAccessible(true);
        ArrayListProductDao productDao = (ArrayListProductDao) field.get(servlet);

        assertNotNull(productDao);
    }
}
