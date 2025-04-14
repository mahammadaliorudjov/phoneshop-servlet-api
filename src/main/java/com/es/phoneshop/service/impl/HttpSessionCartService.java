package com.es.phoneshop.service.impl;

import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.exception.OutOfStockException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.CartService;
import com.es.phoneshop.utils.impl.ReadWriteLockWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.Optional;

public class HttpSessionCartService implements CartService {
    private static final String OUT_OF_STOCK_EXCEPTION_MESSAGE = "Insufficient stock available. Please adjust your quantity";
    private static final String CART_SESSION_ATTRIBUTE = HttpSessionCartService.class.getName() + ".cart";
    private final ReadWriteLockWrapper readWriteLock;
    private final ProductDao productDao;
    private static volatile HttpSessionCartService instance;

    private HttpSessionCartService() {
        productDao = ArrayListProductDao.getInstance();
        readWriteLock = new ReadWriteLockWrapper();
    }

    public static HttpSessionCartService getInstance() {
        if (instance == null) {
            synchronized (HttpSessionCartService.class) {
                if (instance == null) {
                    instance = new HttpSessionCartService();
                }
            }
        }
        return instance;
    }

    @Override
    public void add(Cart cart, Long productId, int quantity) {
        readWriteLock.write(() -> {
            Product product = productDao.get(productId);
            Optional<CartItem> cartItem = findCartItem(cart, product);
            cartItem.ifPresentOrElse(
                    item -> increaseQuantityOfCartItem(item, quantity),
                    () -> createCartItem(cart, product, quantity)
            );
            recalculateCart(cart);
        });
    }

    @Override
    public void update(Cart cart, Long productId, int quantity) {
        readWriteLock.write(() -> {
            Product product = productDao.get(productId);
            CartItem cartItem = findCartItem(cart, product).get();
            int stock = cartItem.getProduct().getStock();
            if (quantity > stock) {
                throw new OutOfStockException(OUT_OF_STOCK_EXCEPTION_MESSAGE);
            }
            cartItem.setQuantity(quantity);
            recalculateCart(cart);
        });
    }

    @Override
    public void delete(Cart cart, Long productId) {
        readWriteLock.write(() -> {
            Product product = productDao.get(productId);
            cart.getCartItems().removeIf(item -> item.getProduct().equals(product));
            recalculateCart(cart);
        });
    }

    @Override
    public Cart getCart(HttpServletRequest request) {
        Cart cart = readWriteLock.read(() -> {
            HttpSession session = request.getSession();
            return (Cart) session.getAttribute(CART_SESSION_ATTRIBUTE);
        });
        if (cart == null) {
            cart = createCartIfAbsent(request);
        }
        return cart;
    }

    @Override
    public void clearCart(Cart cart, HttpSession session) {
        cart.getCartItems().clear();
        session.removeAttribute(CART_SESSION_ATTRIBUTE);
        recalculateCart(cart);
    }

    private void createCartItem(Cart cart, Product product, int quantity) {
        int remainingStock = product.getStock() - quantity;
        if (remainingStock < 0) {
            throw new OutOfStockException(OUT_OF_STOCK_EXCEPTION_MESSAGE);
        }
        cart.getCartItems().add(new CartItem(product, quantity));
    }

    private void recalculateCart(Cart cart) {
        int totalQuantity = cart.getCartItems().stream().
                mapToInt(CartItem::getQuantity)
                .sum();
        cart.setTotalQuantity(totalQuantity);
        BigDecimal totalCost = cart.getCartItems().stream()
                .map(cartItem -> cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalCost(totalCost);
    }

    private Cart createCartIfAbsent(HttpServletRequest request) {
        return readWriteLock.writeWithReturn(() -> {
            HttpSession session = request.getSession();
            Cart сart = (Cart) session.getAttribute(CART_SESSION_ATTRIBUTE);
            if (сart == null) {
                сart = new Cart();
                session.setAttribute(CART_SESSION_ATTRIBUTE, сart);
            }
            return сart;
        });
    }

    public void increaseQuantityOfCartItem(CartItem cartItem, int desiredQuantity) {
        int newQuantity = cartItem.getQuantity() + desiredQuantity;
        int stock = cartItem.getProduct().getStock();
        if (desiredQuantity > stock) {
            throw new OutOfStockException(OUT_OF_STOCK_EXCEPTION_MESSAGE);
        }
        cartItem.setQuantity(newQuantity);
    }

    public Optional<CartItem> findCartItem(Cart cart, Product product) {
        return cart.getCartItems().stream()
                .filter(item -> product.equals(item.getProduct()))
                .findAny();
    }
}
