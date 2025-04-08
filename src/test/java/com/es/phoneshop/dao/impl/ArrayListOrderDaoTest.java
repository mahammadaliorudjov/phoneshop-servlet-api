package com.es.phoneshop.dao.impl;


import com.es.phoneshop.exception.OrderNotFoundException;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.utils.EntityValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArrayListOrderDaoTest {
    private static final long ORDER_ID = 1L;
    private static final long NON_EXISTING_ORDER_ID = 999L;
    private static final String SECURE_ID = "secure123";
    private static final String ORDER2_SECURE_ID = "secure456";
    private static final String NON_EXISTENT_SECURE_ID = "nonexistent";
    private static final String ORDER_EXCEPTION_MESSAGE = "Order with following id is not found: ";
    private static final String INSTANCE = "instance";
    private static final String EXCEPTION_MESSAGE = "SecureId cannot be null";
    private static final int INSTANCE_AMOUNT = 1;
    private static final int ORDERS_SIZE = 1;
    private static final int UPDATED_ORDERS_SIZE = 2;
    private static final int ORDERS_DAO_SIZE = 1;
    private static final int EMPTY_ORDERS_DAO_SIZE = 0;
    private static final int ZERO_INDEX = 0;
    private Order order;
    private ArrayListOrderDao orderDao;
    @Mock
    private Order updatedOrder;
    @Mock
    private Order invalidOrder;
    @Mock
    private Order order2;

    @Before
    public void setUp() {
        resetSingleton();

        order = new Order();
        order.setSecureId(SECURE_ID);
        orderDao = ArrayListOrderDao.getInstance();
    }

    @After
    public void tearDown() {
        resetSingleton();
    }

    private void resetSingleton() {
        try {
            Field instance = ArrayListOrderDao.class.getDeclaredField(INSTANCE);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void testGetInstance() {
        ArrayListOrderDao instance1 = ArrayListOrderDao.getInstance();
        ArrayListOrderDao instance2 = ArrayListOrderDao.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void testGetAllOrdersEmpty() {
        List<Order> orders = orderDao.getAllOrders();

        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetAllOrdersWithData() {
        orderDao.save(order);
        List<Order> orders = orderDao.getAllOrders();

        assertNotNull(orders);
        assertEquals(ORDERS_SIZE, orders.size());
        assertSame(order, orders.get(ZERO_INDEX));
    }

    @Test
    public void testGetOrderBySecureId() {
        orderDao.save(order);

        Order retrievedOrder = orderDao.getOrderBySecureId(SECURE_ID);

        assertNotNull(retrievedOrder);
        assertSame(order, retrievedOrder);
    }

    @Test(expected = OrderNotFoundException.class)
    public void testGetOrderBySecureIdNonExistent() {
        orderDao.getOrderBySecureId(NON_EXISTENT_SECURE_ID);
    }

    @Test
    public void testGetOrderBySecureIdAfterDelete() {
        orderDao.save(order);
        orderDao.delete(ORDER_ID);

        try {
            orderDao.getOrderBySecureId(SECURE_ID);
        } catch (OrderNotFoundException e) {
            assertEquals(ORDER_EXCEPTION_MESSAGE + SECURE_ID, e.getMessage());
        }
    }

    @Test
    public void testSaveNewOrder() {
        Order newOrder = new Order();

        orderDao.save(newOrder);

        assertEquals(ORDERS_DAO_SIZE, orderDao.getAllOrders().size());
    }

    @Test
    public void testSaveExistingOrder() {
        orderDao.save(order);
        when(updatedOrder.getId()).thenReturn(ORDER_ID);

        orderDao.save(updatedOrder);

        List<Order> orders = orderDao.getAllOrders();
        assertEquals(ORDERS_SIZE, orders.size());
        assertSame(updatedOrder, orders.get(ZERO_INDEX));
    }

    @Test
    public void testDelete() {
        orderDao.save(order);
        orderDao.delete(ORDER_ID);

        assertEquals(ORDERS_DAO_SIZE, orderDao.getAllOrders().size());
        assertEquals(EMPTY_ORDERS_DAO_SIZE, orderDao.getAllOrders().size());
    }

    @Test
    public void testDeleteNonExistent() {
        orderDao.save(order);
        orderDao.delete(NON_EXISTING_ORDER_ID);

        assertEquals(ORDERS_DAO_SIZE, orderDao.getAllOrders().size());
        assertEquals(ORDERS_DAO_SIZE, orderDao.getAllOrders().size());
    }

    @Test
    public void testGet() {
        orderDao.save(order);

        Order retrievedOrder = orderDao.get(ORDER_ID);

        assertNotNull(retrievedOrder);
        assertSame(order, retrievedOrder);
    }

    @Test(expected = OrderNotFoundException.class)
    public void testGetNonExistent() {
        orderDao.get(NON_EXISTING_ORDER_ID);
    }

    @Test
    public void testMultipleOrders() {
        orderDao.save(order);

        when(order2.getId()).thenReturn(null);
        when(order2.getSecureId()).thenReturn(ORDER2_SECURE_ID);

        orderDao.save(order2);

        List<Order> orders = orderDao.getAllOrders();

        Order retrieved1 = orderDao.getOrderBySecureId(SECURE_ID);
        Order retrieved2 = orderDao.getOrderBySecureId(ORDER2_SECURE_ID);

        assertEquals(UPDATED_ORDERS_SIZE, orders.size());
        assertSame(order, retrieved1);
        assertSame(order2, retrieved2);
    }

    @Test
    public void testConcurrentGetInstance() throws InterruptedException {
        resetSingleton();

        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        Set<ArrayListOrderDao> instances = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    instances.add(ArrayListOrderDao.getInstance());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await();

        assertEquals(INSTANCE_AMOUNT, instances.size());
    }

    @Test
    public void testValidator() {
        EntityValidator<Order> validator = order -> {
            if (order.getSecureId() == null) {
                throw new IllegalArgumentException(EXCEPTION_MESSAGE);
            }
        };

        orderDao.setValidator(validator);

        orderDao.save(order);

        when(invalidOrder.getSecureId()).thenReturn(null);

        try {
            orderDao.save(invalidOrder);
        } catch (IllegalArgumentException e) {
            assertEquals(EXCEPTION_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void testExceptionFactoryOverride() {
        try {
            orderDao.get(NON_EXISTING_ORDER_ID);
        } catch (OrderNotFoundException e) {
            assertTrue(e.getMessage().contains(ORDER_EXCEPTION_MESSAGE));
        }
    }
}
