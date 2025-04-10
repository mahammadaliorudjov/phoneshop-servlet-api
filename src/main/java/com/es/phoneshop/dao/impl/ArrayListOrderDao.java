package com.es.phoneshop.dao.impl;

import com.es.phoneshop.dao.AbstractDao;
import com.es.phoneshop.dao.OrderDao;
import com.es.phoneshop.exception.OrderNotFoundException;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.utils.impl.ReadWriteLockWrapper;

import java.util.ArrayList;
import java.util.List;

public class ArrayListOrderDao extends AbstractDao<Order> implements OrderDao {
    private static final String ORDER_EXCEPTION_MESSAGE = "Order with following id is not found: ";
    private static volatile ArrayListOrderDao instance;
    private final ReadWriteLockWrapper readWriteLock;

    private ArrayListOrderDao() {
        items = new ArrayList<>();
        readWriteLock = new ReadWriteLockWrapper();
        setExceptionProvider(id ->
                new OrderNotFoundException(ORDER_EXCEPTION_MESSAGE + id)
        );
    }

    public static ArrayListOrderDao getInstance() {
        if (instance == null) {
            synchronized (ArrayListOrderDao.class) {
                if (instance == null) {
                    instance = new ArrayListOrderDao();
                }
            }
        }
        return instance;
    }

    @Override
    public List<Order> getAllOrders() {
        return readWriteLock.read(() -> items);
    }

    @Override
    public Order getOrderBySecureId(String secureId) {
        return readWriteLock.read(() -> items.stream()
                .filter(order -> order.getSecureId().equals(secureId))
                .findAny()
                .orElseThrow(() -> new OrderNotFoundException(ORDER_EXCEPTION_MESSAGE + secureId)));
    }
}
