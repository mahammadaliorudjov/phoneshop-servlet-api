package com.es.phoneshop.web;

import com.es.phoneshop.enums.PaymentMethod;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.service.CartService;
import com.es.phoneshop.service.OrderService;
import com.es.phoneshop.service.impl.DefaultOrderService;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import com.es.phoneshop.utils.impl.OrderValidator;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutPageServlet extends HttpServlet {
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
    private static final String OVERVIEW_SERVLET_PATH = "/order/overview/";
    private static final String FORM_PARAMS = "formParams";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private CartService cartService;
    private OrderService orderService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        cartService = HttpSessionCartService.getInstance();
        orderService = DefaultOrderService.getInstance();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Cart cart = cartService.getCart(request);
        Order order = orderService.getOrder(cart);
        request.setAttribute(ORDER, order);
        List<PaymentMethod> paymentMethods = orderService.getPaymentMethods();
        request.setAttribute(PAYMENT_METHODS, paymentMethods);
        request.getRequestDispatcher(JSP_PATH).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> formParams = new HashMap<>();
        setFormParameters(request, formParams);
        Cart cart = cartService.getCart(request);
        Order order = orderService.getOrder(cart);
        HttpSession session = request.getSession();
        Map<String, String> errors = OrderValidator.builder()
                .firstName(request.getParameter(FIRST_NAME))
                .lastName(request.getParameter(LAST_NAME))
                .deliveryDate(request.getParameter(DELIVERY_DATE))
                .phone(request.getParameter(PHONE))
                .paymentMethod(request.getParameter(PAYMENT_METHOD))
                .deliveryAddress(request.getParameter(DELIVERY_ADDRESS))
                .validateAll()
                .getErrors();
        if (errors.isEmpty()) {
            setOrderDetails(request, order);
            orderService.placeOrder(order);
            cartService.clearCart(cart, session);
            response.sendRedirect(request.getContextPath() + OVERVIEW_SERVLET_PATH + order.getSecureId());
        } else {
            request.setAttribute(FORM_PARAMS, formParams);
            request.setAttribute(ERRORS, errors);
            doGet(request, response);
        }
    }

    private void setOrderDetails(HttpServletRequest request, Order order) {
        order.setFirstName(request.getParameter(FIRST_NAME));
        order.setLastName(request.getParameter(LAST_NAME));
        order.setDeliveryDate(LocalDate.parse(
                request.getParameter(DELIVERY_DATE),
                DateTimeFormatter.ofPattern(DATE_FORMAT)
        ));
        order.setPhone(request.getParameter(PHONE));
        order.setPaymentMethod(PaymentMethod.valueOf(request.getParameter(PAYMENT_METHOD)));
        order.setDeliveryAddress(request.getParameter(DELIVERY_ADDRESS));
    }

    public void setFormParameters(HttpServletRequest request, Map<String, String> formParams) {
        formParams.put(FIRST_NAME, request.getParameter(FIRST_NAME));
        formParams.put(LAST_NAME, request.getParameter(LAST_NAME));
        formParams.put(PHONE, request.getParameter(PHONE));
        formParams.put(DELIVERY_ADDRESS, request.getParameter(DELIVERY_ADDRESS));
        formParams.put(DELIVERY_DATE, request.getParameter(DELIVERY_DATE));
        formParams.put(PAYMENT_METHOD, request.getParameter(PAYMENT_METHOD));
    }
}
