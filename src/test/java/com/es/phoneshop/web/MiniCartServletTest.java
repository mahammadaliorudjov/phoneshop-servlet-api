package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.service.impl.HttpSessionCartService;
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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MiniCartServletTest {
    private static final String CART = "cart";
    private static final String JSP_PATH = "/WEB-INF/pages/miniCart.jsp";
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private HttpSessionCartService cartService;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private Cart cart;
    private MiniCartServlet servlet;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<HttpSessionCartService> mocked = mockStatic(HttpSessionCartService.class)) {
            mocked.when(HttpSessionCartService::getInstance).thenReturn(cartService);
            servlet = new MiniCartServlet();
            servlet.init(servletConfig);
        }

        when(request.getRequestDispatcher(JSP_PATH)).thenReturn(requestDispatcher);
    }

    @Test
    public void doGetSetsCartAttributeAndIncludesJsp() throws Exception {
        when(cartService.getCart(request)).thenReturn(cart);

        servlet.doGet(request, response);

        verify(request).setAttribute(CART, cart);
        verify(request).getRequestDispatcher(JSP_PATH);
        verify(requestDispatcher).include(request, response);
    }

    @Test
    public void doGetHandlesNullCart() throws Exception {
        when(cartService.getCart(request)).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).setAttribute(CART, null);
        verify(requestDispatcher).include(request, response);
    }
}
