package com.es.phoneshop.dao;

import com.es.phoneshop.model.order.Order;

import java.util.List;

public interface OrderDao {
    Order get(Long id);
    List<Order> getAllOrders();
    public Order getOrderBySecureId(String secureId);
    void save(Order order);
}
