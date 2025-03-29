package com.es.phoneshop.web;

import com.es.phoneshop.exception.OutOfStockException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.service.CartService;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import com.es.phoneshop.utils.LocaleSensitiveNumberParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CartPageServlet extends HttpServlet {
    private static final String ERRORS = "errors";
    private static final String QUANTITY = "quantity";
    private static final String CART = "cart";
    private static final String PRODUCT_ID = "productId";
    private static final String JSP_PATH = "/WEB-INF/pages/cart.jsp";
    private static final String ERROR_INVALID_VALUE_MESSAGE = "Invalid value. Please write a valid value";
    private static final String SUCCESS_MESSAGE = "?message=Cart updated successfully";
    private static final String SERVLET_PATH = "/cart";
    private LocaleSensitiveNumberParser parser;
    private CartService cartService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        cartService = HttpSessionCartService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Cart cart = cartService.getCart(request);
        request.setAttribute(CART, cart);
        request.getRequestDispatcher(JSP_PATH).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] quantities = request.getParameterValues(QUANTITY);
        String[] productIds = request.getParameterValues(PRODUCT_ID);
        Map<Long, String> errors = new HashMap<>();
        Cart cart = cartService.getCart(request);
        parser = new LocaleSensitiveNumberParser(request.getLocale());
        for (int i = 0; i < quantities.length; i++) {
            long productId = Long.parseLong(productIds[i]);
            if (!parser.isIntegerAndPositive(quantities[i], request.getLocale())) {
                errors.put(productId, ERROR_INVALID_VALUE_MESSAGE);
                continue;
            }
            int quantity = parser.parseInt(quantities[i]);
            try {
                cartService.update(cart, productId, quantity);
            } catch (OutOfStockException e) {
                errors.put(productId, e.getMessage());
            }
        }
        request.setAttribute(CART, cart);
        if (errors.isEmpty()) {
            response.sendRedirect(request.getContextPath() + SERVLET_PATH + SUCCESS_MESSAGE);
        } else {
            request.setAttribute(ERRORS, errors);
            doGet(request, response);
        }
    }
}
