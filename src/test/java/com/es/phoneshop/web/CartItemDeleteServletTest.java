package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.service.impl.HttpSessionCartService;
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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CartItemDeleteServletTest {
    private static final Long PRODUCT_ID = 1L;
    private static final String VALID_PATH_INFO = "/1";
    private static final String SUCCESS_MESSAGE = "?message=Item deleted successfully";
    private static final String SERVLET_PATH = "/cart";
    private static final String INVALID_PATH_INFO = "/abc";
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private HttpSessionCartService cartService;
    @Mock
    private Cart cart;
    private CartItemDeleteServlet servlet;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<HttpSessionCartService> mocked = mockStatic(HttpSessionCartService.class)) {
            mocked.when(HttpSessionCartService::getInstance).thenReturn(cartService);
            servlet = new CartItemDeleteServlet();
            servlet.init(servletConfig);
        }
    }

    @Test
    public void doPostValidProductIdDeletesItemAndRedirects() throws Exception {
        when(request.getPathInfo()).thenReturn(VALID_PATH_INFO);
        when(request.getContextPath()).thenReturn("");
        when(cartService.getCart(request)).thenReturn(cart);

        servlet.doPost(request, response);

        verify(cartService).delete(cart, PRODUCT_ID);
        verify(response).sendRedirect(SERVLET_PATH + SUCCESS_MESSAGE);
    }

    @Test(expected = NumberFormatException.class)
    public void doPostInvalidProductIdThrowsNumberFormatException() throws Exception {
        when(request.getPathInfo()).thenReturn(INVALID_PATH_INFO);

        servlet.doPost(request, response);
    }
}
