package com.es.phoneshop.web;

import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.exception.OutOfStockException;
import com.es.phoneshop.exception.ProductNotFoundException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import com.es.phoneshop.service.impl.RecentlyViewedProductsServiceImpl;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductDetailsPageServletTest {
    private static final long PRODUCT_ID = 1L;
    private static final String QUANTITY_VALUE_STRING = "2";
    private static final int QUANTITY_VALUE_INT = 2;
    private static final String QUANTITY_EXCEEDS_STOCK_VALUE_STRING = "5000";
    private static final String PATH_INFO = "/1";
    private static final String CONTEXT_PATH = "/context";
    private static final String REDIRECT_PATH = "/context/products/1?message=Product added to cart";
    private static final String INVALID_PATH_INFO = "/100";
    private static final long NON_EXISTING_PRODUCT_ID = 100L;
    private static final String QUANTITY = "quantity";
    private static final String INVALID_QUANTITY_VALUE = "abc";
    private static final String JSP_PATH = "/WEB-INF/pages/productDetails.jsp";
    private static final String ERROR = "error";
    private static final String ERROR_INVALID_VALUE_MESSAGE = "Invalid value. Please write a valid value";
    private static final String OUT_OF_STOCK_EXCEPTION_MESSAGE = "Insufficient stock available. Please adjust your quantity";
    private ProductDetailsPageServlet servlet;
    @Mock
    private Product product;
    @Mock
    private ArrayListProductDao productDao;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private RecentlyViewedProductsServiceImpl recentlyViewedProductsService;
    @Mock
    private HttpSessionCartService cartService;
    @Mock
    private Cart cart;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<ArrayListProductDao> mockedDao = mockStatic(ArrayListProductDao.class);
             MockedStatic<HttpSessionCartService> mockedCart = mockStatic(HttpSessionCartService.class);
             MockedStatic<RecentlyViewedProductsServiceImpl> mockedViewed = mockStatic(RecentlyViewedProductsServiceImpl.class)) {

            mockedDao.when(ArrayListProductDao::getInstance).thenReturn(productDao);
            mockedCart.when(HttpSessionCartService::getInstance).thenReturn(cartService);
            mockedViewed.when(RecentlyViewedProductsServiceImpl::getInstance).thenReturn(recentlyViewedProductsService);

            servlet = new ProductDetailsPageServlet();
            servlet.init(servletConfig);
        }
        when(request.getLocale()).thenReturn(Locale.US);
        when(cartService.getCart(any())).thenReturn(cart);
    }

    @Test
    public void testDoPostValidQuantity() throws ServletException, IOException {
        when(request.getParameter(QUANTITY)).thenReturn(QUANTITY_VALUE_STRING);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(request.getContextPath()).thenReturn(CONTEXT_PATH);

        servlet.doPost(request, response);

        verify(cartService).add(eq(cart), eq(PRODUCT_ID), eq(QUANTITY_VALUE_INT));
        verify(response).sendRedirect(REDIRECT_PATH);
    }

    @Test
    public void testDoPostNonNumericQuantity() throws ServletException, IOException {
        when(request.getParameter(QUANTITY)).thenReturn(INVALID_QUANTITY_VALUE);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(request.getRequestDispatcher(JSP_PATH)).thenReturn(requestDispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq(ERROR), eq(ERROR_INVALID_VALUE_MESSAGE));
        verify(request).getRequestDispatcher(JSP_PATH);
        verify(requestDispatcher).forward(request, response);
        verify(cartService, never()).add(any(), anyLong(), anyInt());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    public void testDoPostWhenQuantityExceedsStockShowsError() throws ServletException, IOException {
        when(request.getParameter(QUANTITY)).thenReturn(QUANTITY_EXCEEDS_STOCK_VALUE_STRING);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(request.getRequestDispatcher(JSP_PATH)).thenReturn(requestDispatcher);
        doThrow(new OutOfStockException(OUT_OF_STOCK_EXCEPTION_MESSAGE))
                .when(cartService).add(any(Cart.class), eq(PRODUCT_ID), eq(Integer.parseInt(QUANTITY_EXCEEDS_STOCK_VALUE_STRING)));

        servlet.doPost(request, response);

        verify(request).setAttribute(eq(ERROR), any());
        verify(request).getRequestDispatcher(JSP_PATH);
        verify(requestDispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    public void testDoGetExistingProduct() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(productDao.getProduct(PRODUCT_ID)).thenReturn(product);
        when(request.getRequestDispatcher(JSP_PATH)).thenReturn(requestDispatcher);

        servlet.doGet(request, response);
    }

    @Test(expected = ProductNotFoundException.class)
    public void testDoGetNonExistingProductThrowsException() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(INVALID_PATH_INFO);
        when(productDao.getProduct(NON_EXISTING_PRODUCT_ID))
                .thenThrow(new ProductNotFoundException(""));

        servlet.doGet(request, response);
    }
}
