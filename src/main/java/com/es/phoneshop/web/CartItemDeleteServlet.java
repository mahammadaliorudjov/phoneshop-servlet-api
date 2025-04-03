package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.service.CartService;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CartItemDeleteServlet extends HttpServlet {
    private static final int PATH_ID_START_INDEX = 1;
    private static final String SERVLET_PATH = "/cart";
    private static final String SUCCESS_MESSAGE = "?message=Item deleted successfully";
    private CartService cartService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        cartService = HttpSessionCartService.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long id = Long.parseLong(request.getPathInfo().substring(PATH_ID_START_INDEX));
        Cart cart = cartService.getCart(request);
        cartService.delete(cart, id);
        response.sendRedirect(request.getContextPath() + SERVLET_PATH + SUCCESS_MESSAGE);
    }
}
