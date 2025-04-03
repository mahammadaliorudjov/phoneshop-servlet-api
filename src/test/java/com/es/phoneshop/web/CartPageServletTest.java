package com.es.phoneshop.web;

import com.es.phoneshop.exception.OutOfStockException;
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

import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CartPageServletTest {
    private static final String CART = "cart";
    private static final String QUANTITY = "quantity";
    private static final String ERRORS = "errors";
    private static final String PRODUCT_ID = "productId";
    private static final String SUCCESS_URL = "/cart?message=Cart updated successfully";
    private static final Long PRODUCT_ID_LONG = 1L;
    private static final Long PRODUCT2_ID_LONG = 2L;
    private static final int QUANTITY_VALUE = 2;
    private static final int QUANTITY2_VALUE = 3;
    private static final String[] QUANTITIES = {"2", "3"};
    private static final String[] PRODUCT_IDS = {"1", "2"};
    ;

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
    private CartPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<HttpSessionCartService> mocked = mockStatic(HttpSessionCartService.class)) {
            mocked.when(HttpSessionCartService::getInstance).thenReturn(cartService);
            servlet = new CartPageServlet();
            servlet.init(servletConfig);
        }
        when(cartService.getCart(request)).thenReturn(cart);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getLocale()).thenReturn(Locale.US);
    }

    @Test
    public void doGetSetsCartAttributeAndForwards() throws Exception {
        when(cartService.getCart(request)).thenReturn(cart);

        servlet.doGet(request, response);

        verify(request).setAttribute(CART, cart);
        verify(request).getRequestDispatcher(anyString());
    }

    @Test
    public void doPostValidQuantitiesRedirectsWithSuccess() throws Exception {
        when(request.getParameterValues(QUANTITY)).thenReturn(QUANTITIES);
        when(request.getParameterValues(PRODUCT_ID)).thenReturn(PRODUCT_IDS);
        when(cartService.getCart(request)).thenReturn(cart);

        servlet.doPost(request, response);

        verify(response).sendRedirect(request.getContextPath() + SUCCESS_URL);
        verify(cartService).update(cart, PRODUCT_ID_LONG, QUANTITY_VALUE);
        verify(cartService).update(cart, PRODUCT2_ID_LONG, QUANTITY2_VALUE);
    }

    @Test
    public void doPostOutOfStockExceptionAddsError() throws Exception {
        when(request.getParameterValues(QUANTITY)).thenReturn(QUANTITIES);
        when(request.getParameterValues(PRODUCT_ID)).thenReturn(PRODUCT_IDS);
        doThrow(new OutOfStockException("")).when(cartService).update(cart, PRODUCT_ID_LONG, QUANTITY_VALUE);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq(ERRORS), argThat((Map<Long, String> errors) ->
                errors.containsKey(PRODUCT_ID_LONG) && errors.get(PRODUCT_ID_LONG).isEmpty()
        ));
    }
}
