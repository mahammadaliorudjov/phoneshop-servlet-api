package com.es.phoneshop.web;

import com.es.phoneshop.enums.PaymentMethod;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.service.impl.DefaultOrderService;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutPageServletTest {
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PHONE = "phone";
    private static final String DELIVERY_ADDRESS = "deliveryAddress";
    private static final String DELIVERY_DATE = "deliveryDate";
    private static final String PAYMENT_METHOD = "paymentMethod";
    private static final String ERRORS = "errors";
    private static final String ORDER = "order";
    private static final String PAYMENT_METHODS = "paymentMethods";
    private static final String JSP_PATH = "/WEB-INF/pages/checkout.jsp";
    private static final String SERVLET_PATH = "/checkout";
    private static final String OVERVIEW_SERVLET_PATH = "/order/overview/";
    private static final String ERROR_INVALID_VALUE_MESSAGE = "Invalid value. Please write a valid value";
    private static final String FORM_PARAMS = "formParams";
    private static final String CHECKOUT_ERRORS = "checkoutErrors";
    private static final String VALID_FIRST_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";
    private static final String VALID_PHONE = "+375123456789";
    private static final String VALID_ADDRESS = "Test Address";
    private static final String VALID_PAYMENT_METHOD = "CREDIT_CARD";
    private static final String INVALID_FIRST_NAME = "John123";
    private static final String INVALID_LAST_NAME = "Doe456";
    private static final String INVALID_PHONE = "1234567890";
    private static final String EMPTY_STRING = "";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String INVALID_DATE_FORMAT = "01/01/2025";
    private static final String SECURE_ID = "12345";
    private static final String INVALID_DELIVERY_DATE = "2020-01-01";
    private static final String VALID_DELIVERY_DATE = "2025-05-01";
    private static final int DAYS_TO_ADD = 1;
    private static final int ERRORS_SIZE = 6;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private HttpSessionCartService cartService;
    @Mock
    private DefaultOrderService orderService;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private Cart cart;
    @Mock
    private Order order;

    private CheckoutPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<HttpSessionCartService> cartServiceMocked = mockStatic(HttpSessionCartService.class);
             MockedStatic<DefaultOrderService> orderServiceMocked = mockStatic(DefaultOrderService.class)
        ) {
            cartServiceMocked.when(HttpSessionCartService::getInstance).thenReturn(cartService);
            orderServiceMocked.when(DefaultOrderService::getInstance).thenReturn(orderService);
            servlet = new CheckoutPageServlet();
            servlet.init(servletConfig);
        }
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(JSP_PATH)).thenReturn(requestDispatcher);
        when(cartService.getCart(request)).thenReturn(cart);
        when(orderService.getOrder(cart)).thenReturn(order);
        when(orderService.getPaymentMethods()).thenReturn(Arrays.asList(PaymentMethod.values()));
        when(request.getContextPath()).thenReturn("");
    }

    @Test
    public void testDoGetWithoutErrors() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).setAttribute(ORDER, order);
        verify(request).setAttribute(PAYMENT_METHODS, orderService.getPaymentMethods());
        verify(request, never()).setAttribute(eq(ERRORS), any());
        verify(request, never()).setAttribute(eq(FORM_PARAMS), any());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoGetWithErrors() throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();
        errors.put(FIRST_NAME, ERROR_INVALID_VALUE_MESSAGE);

        Map<String, String> formParams = new HashMap<>();
        formParams.put(FIRST_NAME, INVALID_FIRST_NAME);

        servlet.doGet(request, response);

        verify(request).setAttribute(ORDER, order);
        verify(request).setAttribute(PAYMENT_METHODS, orderService.getPaymentMethods());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoPostAllValid() throws ServletException, IOException {
        LocalDate tomorrow = LocalDate.now().plusDays(DAYS_TO_ADD);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);
        when(order.getSecureId()).thenReturn(SECURE_ID);

        servlet.doPost(request, response);

        verify(order).setFirstName(VALID_FIRST_NAME);
        verify(order).setLastName(VALID_LAST_NAME);
        verify(order).setPhone(VALID_PHONE);
        verify(order).setDeliveryAddress(VALID_ADDRESS);
        verify(order).setDeliveryDate(tomorrow);
        verify(order).setPaymentMethod(PaymentMethod.CREDIT_CARD);
        verify(orderService).placeOrder(order);
        verify(cartService).clearCart(cart, session);
        verify(response).sendRedirect(OVERVIEW_SERVLET_PATH + SECURE_ID);
        verify(session, never()).setAttribute(eq(CHECKOUT_ERRORS), any());
    }

    @Test
    public void testDoPostInvalidFirstName() throws ServletException, IOException {
        LocalDate tomorrow = LocalDate.now().plusDays(DAYS_TO_ADD);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(INVALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(FIRST_NAME));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(FIRST_NAME));
    }

    @Test
    public void testDoPostInvalidLastName() throws ServletException, IOException {
        LocalDate tomorrow = LocalDate.now().plusDays(DAYS_TO_ADD);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(INVALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(LAST_NAME));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(LAST_NAME));
    }

    @Test
    public void testDoPostInvalidPhone() throws ServletException, IOException {
        LocalDate tomorrow = LocalDate.now().plusDays(DAYS_TO_ADD);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(INVALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(PHONE));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(PHONE));
    }

    @Test
    public void testDoPostEmptyDeliveryAddress() throws ServletException, IOException {
        LocalDate tomorrow = LocalDate.now().plusDays(DAYS_TO_ADD);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(EMPTY_STRING);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(DELIVERY_ADDRESS));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(DELIVERY_ADDRESS));
    }

    @Test
    public void testDoPostNullDeliveryAddress() throws ServletException, IOException {
        LocalDate tomorrow = LocalDate.now().plusDays(DAYS_TO_ADD);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(null);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(DELIVERY_ADDRESS));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(DELIVERY_ADDRESS));
    }

    @Test
    public void testDoPostPastDeliveryDate() throws ServletException, IOException {
        LocalDate yesterday = LocalDate.now().minusDays(DAYS_TO_ADD);
        String formattedDate = yesterday.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(DELIVERY_DATE));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(DELIVERY_DATE));
    }

    @Test
    public void testDoPostInvalidDateFormat() throws ServletException, IOException {
        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(INVALID_DATE_FORMAT);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(DELIVERY_DATE));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(DELIVERY_DATE));
    }

    @Test
    public void testDoPostEmptyPaymentMethod() throws ServletException, IOException {
        LocalDate tomorrow = LocalDate.now().plusDays(DAYS_TO_ADD);
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(formattedDate);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(EMPTY_STRING);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());
        assertTrue(errors.containsKey(PAYMENT_METHOD));
        assertEquals(ERROR_INVALID_VALUE_MESSAGE, errors.get(PAYMENT_METHOD));
    }

    @Test
    public void testDoPostMultipleValidationErrors() throws ServletException, IOException {
        when(request.getParameter(FIRST_NAME)).thenReturn(INVALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(INVALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(INVALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(EMPTY_STRING);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(INVALID_DELIVERY_DATE);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(EMPTY_STRING);

        servlet.doPost(request, response);

        ArgumentCaptor<Map<String, String>> errorsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, String>> formParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(request).setAttribute(eq(ERRORS), errorsCaptor.capture());
        verify(request).setAttribute(eq(FORM_PARAMS), formParamsCaptor.capture());

        Map<String, String> errors = errorsCaptor.getValue();

        Map<String, String> formParams = formParamsCaptor.getValue();

        verify(orderService, never()).placeOrder(any());

        assertEquals(INVALID_FIRST_NAME, formParams.get(FIRST_NAME));
        assertEquals(INVALID_LAST_NAME, formParams.get(LAST_NAME));
        assertEquals(INVALID_PHONE, formParams.get(PHONE));
        assertEquals(EMPTY_STRING, formParams.get(DELIVERY_ADDRESS));
        assertEquals(INVALID_DELIVERY_DATE, formParams.get(DELIVERY_DATE));
        assertEquals(EMPTY_STRING, formParams.get(PAYMENT_METHOD));

        assertEquals(ERRORS_SIZE, errors.size());
        assertTrue(errors.containsKey(FIRST_NAME));
        assertTrue(errors.containsKey(LAST_NAME));
        assertTrue(errors.containsKey(PHONE));
        assertTrue(errors.containsKey(DELIVERY_ADDRESS));
        assertTrue(errors.containsKey(DELIVERY_DATE));
        assertTrue(errors.containsKey(PAYMENT_METHOD));
    }

    @Test
    public void testSetFormParameters() {
        when(request.getParameter(FIRST_NAME)).thenReturn(VALID_FIRST_NAME);
        when(request.getParameter(LAST_NAME)).thenReturn(VALID_LAST_NAME);
        when(request.getParameter(PHONE)).thenReturn(VALID_PHONE);
        when(request.getParameter(DELIVERY_ADDRESS)).thenReturn(VALID_ADDRESS);
        when(request.getParameter(DELIVERY_DATE)).thenReturn(VALID_DELIVERY_DATE);
        when(request.getParameter(PAYMENT_METHOD)).thenReturn(VALID_PAYMENT_METHOD);

        Map<String, String> formParams = new HashMap<>();

        servlet.setFormParameters(request, formParams);

        assertEquals(VALID_FIRST_NAME, formParams.get(FIRST_NAME));
        assertEquals(VALID_LAST_NAME, formParams.get(LAST_NAME));
        assertEquals(VALID_PHONE, formParams.get(PHONE));
        assertEquals(VALID_ADDRESS, formParams.get(DELIVERY_ADDRESS));
        assertEquals(VALID_DELIVERY_DATE, formParams.get(DELIVERY_DATE));
        assertEquals(VALID_PAYMENT_METHOD, formParams.get(PAYMENT_METHOD));
    }
}
