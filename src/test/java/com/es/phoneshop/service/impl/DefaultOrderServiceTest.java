package com.es.phoneshop.service.impl;

import com.es.phoneshop.dao.impl.ArrayListOrderDao;
import com.es.phoneshop.enums.PaymentMethod;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.order.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultOrderServiceTest {
    private static final BigDecimal CART_TOTAL = new BigDecimal("100.00");
    private static final BigDecimal SUBTOTAL = new BigDecimal("199.99");
    private static final BigDecimal DELIVERY_COST = new BigDecimal("5");
    private static final BigDecimal TOTAL_COST = new BigDecimal("105.00");
    private static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String INSTANCE = "instance";
    private static final int INSTANCE_AMOUNT = 1;
    private static final Long ORDER_ID = 123L;
    private static final int ORDER_ITEMS_SIZE = 2;
    private DefaultOrderService orderService;
    private List<CartItem> cartItems;
    @Mock
    private Cart cart;
    @Mock
    private CartItem cartItem1;
    @Mock
    private CartItem cartItem2;
    @Mock
    private CartItem clonedItem1;
    @Mock
    private CartItem clonedItem2;
    @Mock
    private ArrayListOrderDao orderDao;

    @Before
    public void setUp() throws CloneNotSupportedException {
        resetSingleton();

        try (MockedStatic<ArrayListOrderDao> orderDaoMocked = mockStatic(ArrayListOrderDao.class)) {
            orderDaoMocked.when(ArrayListOrderDao::getInstance).thenReturn(orderDao);
            orderService = DefaultOrderService.getInstance();
        }

        cartItems = Arrays.asList(cartItem1, cartItem2);
        when(cart.getCartItems()).thenReturn(cartItems);
        when(cart.getTotalCost()).thenReturn(CART_TOTAL);

        when(cartItem1.clone()).thenReturn(clonedItem1);
        when(cartItem2.clone()).thenReturn(clonedItem2);
    }

    @After
    public void tearDown() {
        resetSingleton();
    }

    private void resetSingleton() {
        try {
            Field instance = DefaultOrderService.class.getDeclaredField(INSTANCE);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void testGetInstance() {
        DefaultOrderService instance1 = DefaultOrderService.getInstance();
        DefaultOrderService instance2 = DefaultOrderService.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void testGetOrder() throws CloneNotSupportedException {
        Order order = orderService.getOrder(cart);
        List<CartItem> orderItems = order.getCartItems();

        assertNotNull(order);
        assertEquals(CART_TOTAL, order.getSubtotal());
        assertEquals(DELIVERY_COST, order.getDeliveryCost());
        assertEquals(TOTAL_COST, order.getTotalCost());

        assertEquals(ORDER_ITEMS_SIZE, orderItems.size());
        assertTrue(orderItems.contains(clonedItem1));
        assertTrue(orderItems.contains(clonedItem2));

        verify(cart).getCartItems();
        verify(cart).getTotalCost();
        verify(cartItem1).clone();
        verify(cartItem2).clone();
    }

    @Test
    public void testGetOrderWithEmptyCart() {
        when(cart.getCartItems()).thenReturn(Collections.emptyList());
        when(cart.getTotalCost()).thenReturn(BigDecimal.ZERO);

        Order order = orderService.getOrder(cart);

        assertNotNull(order);
        assertEquals(BigDecimal.ZERO, order.getSubtotal());
        assertEquals(DELIVERY_COST, order.getDeliveryCost());
        assertEquals(DELIVERY_COST, order.getTotalCost());
        assertTrue(order.getCartItems().isEmpty());
    }

    @Test
    public void testGetPaymentMethods() {
        List<PaymentMethod> paymentMethods = orderService.getPaymentMethods();

        assertNotNull(paymentMethods);
        assertEquals(PaymentMethod.values().length, paymentMethods.size());
        Arrays.stream(PaymentMethod.values())
                .forEach(method -> assertTrue(paymentMethods.contains(method)));
    }

    @Test
    public void testPlaceOrder() {
        Order order = new Order();

        orderService.placeOrder(order);

        assertNotNull(order.getSecureId());
        assertTrue(order.getSecureId().matches(UUID_PATTERN));
        verify(orderDao).save(order);
    }

    @Test
    public void testGetOrderWhenOrderIsNotCloneable() throws CloneNotSupportedException {
        when(cartItem1.clone()).thenThrow(new CloneNotSupportedException());

        try {
            orderService.getOrder(cart);
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CloneNotSupportedException);
        }
    }

    @Test
    public void testConcurrentGetInstance() throws InterruptedException {
        resetSingleton();

        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        Set<DefaultOrderService> instances = Collections.synchronizedSet(new HashSet<>());

        try (MockedStatic<ArrayListOrderDao> orderDaoMocked = mockStatic(ArrayListOrderDao.class)) {
            orderDaoMocked.when(ArrayListOrderDao::getInstance).thenReturn(orderDao);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        instances.add(DefaultOrderService.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            endLatch.await();
        }

        assertEquals(INSTANCE_AMOUNT, instances.size());
    }

    @Test
    public void testTotalCostCalculation() {
        when(cart.getTotalCost()).thenReturn(SUBTOTAL);

        Order order = orderService.getOrder(cart);

        assertEquals(SUBTOTAL, order.getSubtotal());
        assertEquals(DELIVERY_COST, order.getDeliveryCost());
        assertEquals(SUBTOTAL.add(DELIVERY_COST), order.getTotalCost());
    }

    @Test
    public void testOrderSavingAfterPlacement() {
        Order order = new Order();
        order.setId(ORDER_ID);

        orderService.placeOrder(order);

        assertEquals(ORDER_ID, order.getId());
        assertNotNull(order.getSecureId());
        verify(orderDao).save(order);
    }
}
