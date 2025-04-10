package com.es.phoneshop.service;

import com.es.phoneshop.model.cart.Cart;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    void add(Cart cart, Long productId, int quantity);
    void update(Cart cart, Long productId, int quantity);
    void delete(Cart cart, Long productId);
    Cart getCart(HttpServletRequest request);
    void clearCart(Cart cart, HttpSession session);
}
