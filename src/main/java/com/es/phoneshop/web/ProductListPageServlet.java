package com.es.phoneshop.web;

import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.CartService;
import com.es.phoneshop.service.RecentlyViewedProductsService;
import com.es.phoneshop.service.impl.HttpSessionCartService;
import com.es.phoneshop.service.impl.RecentlyViewedProductsServiceImpl;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Deque;
import java.util.Optional;

public class ProductListPageServlet extends HttpServlet {
    private ProductDao productDao;
    private static final String QUERY = "query";
    private static final String SORT_FIELD = "sort";
    private static final String SORT_ORDER = "order";
    private static final String PRODUCTS = "products";
    private static final String JSP_PATH = "/WEB-INF/pages/productList.jsp";
    private static final String QUANTITY = "quantity";
    private static final String CART = "cart";
    private static final String RECENTLY_VIEWED_PRODUCTS = "recentlyViewedProducts";
    private CartService cartService;
    private RecentlyViewedProductsService recentlyViewedProductsService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        productDao = ArrayListProductDao.getInstance();
        recentlyViewedProductsService = RecentlyViewedProductsServiceImpl.getInstance();
        cartService = HttpSessionCartService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter(QUERY);
        String field = request.getParameter(SORT_FIELD);
        String order = request.getParameter(SORT_ORDER);
        SortField sortField = parseEnum(field, SortField.class);
        SortOrder sortOrder = parseEnum(order, SortOrder.class);
        Deque<Product> products = recentlyViewedProductsService.getViewedProducts(request);
        request.setAttribute(PRODUCTS, productDao.findProducts(query, sortField, sortOrder));
        request.setAttribute(CART, cartService.getCart(request));
        request.setAttribute(RECENTLY_VIEWED_PRODUCTS, products);
        request.getRequestDispatcher(JSP_PATH).forward(request, response);
    }

    private <T extends Enum<T>> T parseEnum(String string, Class<T> tClass) {
        return Optional.ofNullable(string)
                .map(String::toUpperCase)
                .map(value -> Enum.valueOf(tClass, value))
                .orElse(null);
    }
}
