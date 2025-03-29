package com.es.phoneshop.service.impl;

import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.exception.OutOfStockException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.product.Product;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HttpSessionCartServiceTest {
    @Mock
    private ArrayListProductDao productDao;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    private Product product;
    private HttpSessionCartService cartService;
    private static final String PRODUCT_DESCRIPTION = "Test Product";
    private static final String PRODUCT_CODE = "TEST_CODE";
    private static final int FIRST_CART_ITEM_INDEX = 0;
    private static final int CART_SIZE = 1;
    private static final String CART_SESSION_ATTRIBUTE = HttpSessionCartService.class.getName() + ".cart";
    private static final Long PRODUCT_ID = 1L;
    private static final Long PRODUCT2_ID = 2L;
    private static final int INITIAL_PRODUCT_STOCK = 10;
    private static final int QUANTITY_TO_ADD = 3;
    private static final int TOTAL_QUANTITY = 9;
    private static final int UPDATED_QUANTITY = QUANTITY_TO_ADD * 2;
    private static final int EXCESSIVE_QUANTITY = 20;
    private static final BigDecimal PRICE = new BigDecimal(200);
    private static final BigDecimal PRODUCT2_PRICE = new BigDecimal(300);
    private static final BigDecimal TOTAL_PRICE = new BigDecimal(2100);

    @Before
    public void setUp() throws Exception {
        try (MockedStatic<ArrayListProductDao> mocked = mockStatic(ArrayListProductDao.class)) {
            mocked.when(ArrayListProductDao::getInstance).thenReturn(productDao);
            cartService = HttpSessionCartService.getInstance();
        }
        when(request.getSession()).thenReturn(session);

        product = new Product();
        product.setId(PRODUCT_ID);
        product.setPrice(PRICE);
        product.setDescription(PRODUCT_DESCRIPTION);
        product.setCode(PRODUCT_CODE);
        product.setStock(INITIAL_PRODUCT_STOCK);

        when(productDao.getProduct(PRODUCT_ID)).thenReturn(product);
    }

    @Test
    public void testAddNewCartItem() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());

        cartService.add(cart, PRODUCT_ID, QUANTITY_TO_ADD);

        assertEquals(CART_SIZE, cart.getCartItems().size());
        assertEquals(QUANTITY_TO_ADD, cart.getCartItems().get(FIRST_CART_ITEM_INDEX).getQuantity());
        assertEquals(product, cart.getCartItems().get(FIRST_CART_ITEM_INDEX).getProduct());
    }

    @Test
    public void testAddExistingCartItemUpdatesQuantity() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());
        CartItem cartItem = new CartItem(product, QUANTITY_TO_ADD);
        cart.getCartItems().add(cartItem);

        cartService.add(cart, PRODUCT_ID, QUANTITY_TO_ADD);

        assertEquals(CART_SIZE, cart.getCartItems().size());
        assertEquals(UPDATED_QUANTITY, cart.getCartItems().get(FIRST_CART_ITEM_INDEX).getQuantity());
    }

    @Test(expected = OutOfStockException.class)
    public void testAddNewCartItemThrowsOutOfStockException() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());

        cartService.add(cart, PRODUCT_ID, EXCESSIVE_QUANTITY);
    }

    @Test(expected = OutOfStockException.class)
    public void testUpdateCartItemThrowsOutOfStockException() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());
        CartItem cartItem = new CartItem(product, QUANTITY_TO_ADD);
        cart.getCartItems().add(cartItem);

        cartService.increaseQuantityOfCartItem(cartItem, EXCESSIVE_QUANTITY);
    }

    @Test
    public void testGetCartReturnsExistingCart() {
        Cart existingCart = new Cart();
        existingCart.setCartItems(new ArrayList<>());
        when(session.getAttribute(CART_SESSION_ATTRIBUTE)).thenReturn(existingCart);

        Cart resultCart = cartService.getCart(request);

        assertNotNull(resultCart);
        assertEquals(existingCart, resultCart);
    }

    @Test
    public void testGetCartCreatesCartIfAbsent() {
        when(session.getAttribute(CART_SESSION_ATTRIBUTE)).thenReturn(null);

        Cart resultCart = cartService.getCart(request);

        assertNotNull(resultCart);
        verify(session).setAttribute(eq(CART_SESSION_ATTRIBUTE), eq(resultCart));
    }

    @Test
    public void testFindCartItemReturnsMatchingItem() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());
        CartItem cartItem = new CartItem(product, QUANTITY_TO_ADD);
        cart.getCartItems().add(cartItem);

        Optional<CartItem> result = cartService.findCartItem(cart, product);

        assertTrue(result.isPresent());
        assertEquals(cartItem, result.get());
    }

    @Test
    public void testFindCartItemReturnsEmptyIfNotFound() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());

        Optional<CartItem> result = cartService.findCartItem(cart, product);

        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateCartItemSuccessfully() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());
        CartItem cartItem = new CartItem(product, QUANTITY_TO_ADD);
        cart.getCartItems().add(cartItem);
        BigDecimal expectedPrice = product.getPrice().multiply(BigDecimal.valueOf(UPDATED_QUANTITY));

        cartService.update(cart, PRODUCT_ID, UPDATED_QUANTITY);

        assertEquals(UPDATED_QUANTITY, cartItem.getQuantity());
        assertEquals(UPDATED_QUANTITY, cart.getTotalQuantity());
        assertEquals(expectedPrice, cart.getTotalCost());
    }

    @Test(expected = OutOfStockException.class)
    public void testUpdateCartItemExceedsStockThrowsException() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());
        CartItem cartItem = new CartItem(product, QUANTITY_TO_ADD);
        cart.getCartItems().add(cartItem);
        int excessiveQuantity = INITIAL_PRODUCT_STOCK + 1;

        cartService.update(cart, PRODUCT_ID, excessiveQuantity);
    }

    @Test
    public void testUpdateRecalculatesCartCorrectly() {
        Product product2 = new Product();
        product2.setId(PRODUCT2_ID);
        product2.setPrice(PRODUCT2_PRICE);
        product2.setStock(INITIAL_PRODUCT_STOCK);
        when(productDao.getProduct(PRODUCT2_ID)).thenReturn(product2);

        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());

        cartService.add(cart, PRODUCT_ID, QUANTITY_TO_ADD);
        cartService.add(cart, PRODUCT2_ID, QUANTITY_TO_ADD);

        cartService.update(cart, PRODUCT_ID, UPDATED_QUANTITY);

        assertEquals(TOTAL_QUANTITY, cart.getTotalQuantity());
        assertEquals(TOTAL_PRICE, cart.getTotalCost());
    }
}
