package com.es.phoneshop.service.impl;

import com.es.phoneshop.dao.OrderDao;
import com.es.phoneshop.dao.impl.ArrayListOrderDao;
import com.es.phoneshop.enums.PaymentMethod;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.service.OrderService;
import com.es.phoneshop.utils.impl.ReadWriteLockWrapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class DefaultOrderService implements OrderService {
    private static final int DELIVERY_COST = 5;
    private static volatile DefaultOrderService instance;
    private final ReadWriteLockWrapper readWriteLock;
    private final OrderDao orderDao;

    private DefaultOrderService() {
        orderDao = ArrayListOrderDao.getInstance();
        readWriteLock = new ReadWriteLockWrapper();
    }

    public static DefaultOrderService getInstance() {
        if (instance == null) {
            synchronized (HttpSessionCartService.class) {
                if (instance == null) {
                    instance = new DefaultOrderService();
                }
            }
        }
        return instance;
    }

    @Override
    public Order getOrder(Cart cart) {
        return readWriteLock.writeWithReturn(() -> {
            Order order = new Order();
            List<CartItem> cartItems = cart.getCartItems().stream()
                    .map(this::cloneItem)
                    .toList();
            order.setCartItems(cartItems);
            order.setSubtotal(cart.getTotalCost());
            order.setDeliveryCost(calculateDeliveryCost());
            BigDecimal totalCost = order.getSubtotal().add(order.getDeliveryCost());
            order.setTotalCost(totalCost);
            return order;
        });
    }

    @Override
    public List<PaymentMethod> getPaymentMethods() {
        return readWriteLock.read(() ->
                List.of(PaymentMethod.values()
                ));
    }

    @Override
    public void placeOrder(Order order) {
        readWriteLock.write(() -> {
            order.setSecureId(UUID.randomUUID().toString());
            orderDao.save(order);
        });
    }

    private BigDecimal calculateDeliveryCost() {
        return new BigDecimal(DELIVERY_COST);
    }

    private CartItem cloneItem(CartItem item) {
        try {
            return (CartItem) item.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
