package com.es.phoneshop.web;

import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.exception.OutOfStockException;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.CartService;
import com.es.phoneshop.service.RecentlyViewedProductsService;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import com.es.phoneshop.service.impl.RecentlyViewedProductsServiceImpl;
import com.es.phoneshop.utils.impl.LocaleSensitiveNumberParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Deque;

public class ProductDetailsPageServlet extends HttpServlet {
    private static final String PRODUCT = "product";
    private static final String ERROR = "error";
    private static final String QUANTITY = "quantity";
    private static final String CART = "cart";
    private static final String RECENTLY_VIEWED_PRODUCTS = "recentlyViewedProducts";
    private static final String ERROR_INVALID_VALUE_MESSAGE = "Invalid value. Please write a valid value";
    private static final String OUT_OF_STOCK_EXCEPTION_MESSAGE = "Insufficient stock available. Please adjust your quantity";
    private static final String SUCCESS_MESSAGE_PARAMETER = "message=Product added to cart";
    private static final String SERVLET_PATH = "/products/";
    private static final int PATH_ID_START_INDEX = 1;
    private static final String JSP_PATH = "/WEB-INF/pages/productDetails.jsp";
    private ProductDao productDao;
    private CartService cartService;
    private RecentlyViewedProductsService recentlyViewedProductsService;
    private LocaleSensitiveNumberParser parser;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        productDao = ArrayListProductDao.getInstance();
        cartService = HttpSessionCartService.getInstance();
        recentlyViewedProductsService = RecentlyViewedProductsServiceImpl.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long id = Long.parseLong(request.getPathInfo().substring(PATH_ID_START_INDEX));
        Product product = productDao.get(id);
        recentlyViewedProductsService.addViewedProduct(request, product);
        Deque<Product> products = recentlyViewedProductsService.getViewedProducts(request);
        request.setAttribute(PRODUCT, product);
        request.setAttribute(CART, cartService.getCart(request));
        request.setAttribute(RECENTLY_VIEWED_PRODUCTS, products);
        request.getRequestDispatcher(JSP_PATH).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String quantityString = request.getParameter(QUANTITY);
        Long productId = Long.parseLong(request.getPathInfo().substring(PATH_ID_START_INDEX));
        parser = new LocaleSensitiveNumberParser(request.getLocale());
        if (!parser.isIntegerAndPositive(quantityString, request.getLocale())) {
            request.setAttribute(ERROR, ERROR_INVALID_VALUE_MESSAGE);
            request.setAttribute(QUANTITY, quantityString);
            doGet(request, response);
            return;
        }
        int quantity = parser.parseInt(quantityString);
        try {
            Cart cart = cartService.getCart(request);
            cartService.add(cart, productId, quantity);
        } catch (OutOfStockException e) {
            request.setAttribute(ERROR, OUT_OF_STOCK_EXCEPTION_MESSAGE);
            doGet(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + SERVLET_PATH + productId + "?" + SUCCESS_MESSAGE_PARAMETER);
    }
}
