package com.es.phoneshop.service;

import com.es.phoneshop.enums.PaymentMethod;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.cart.Cart;

import java.util.List;

public interface OrderService {
    void placeOrder(Order order);
    Order getOrder(Cart cart);
    List<PaymentMethod> getPaymentMethods();
}
