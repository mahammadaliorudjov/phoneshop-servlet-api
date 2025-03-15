package com.es.phoneshop.web;

import com.es.phoneshop.dao.ArrayListProductDao;
import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public class ProductListPageServlet extends HttpServlet {
    private ProductDao productDao;
    private static final String QUERY = "query";
    private static final String SORT_FIELD = "sort";
    private static final String SORT_ORDER = "order";
    private static final String PRODUCTS = "products";
    private static final String JSP_PATH = "/WEB-INF/pages/productList.jsp";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        productDao = ArrayListProductDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter(QUERY);
        String field = request.getParameter(SORT_FIELD);
        String order = request.getParameter(SORT_ORDER);
        SortField sortField = parseEnum(field, SortField.class);
        SortOrder sortOrder = parseEnum(order, SortOrder.class);
        request.setAttribute(PRODUCTS, productDao.findProducts(query, sortField, sortOrder));
        request.getRequestDispatcher(JSP_PATH).forward(request, response);

    }

    private <T extends Enum<T>> T parseEnum(String string, Class<T> tClass) {
        return Optional.ofNullable(string)
                .map(String::toUpperCase)
                .map(value -> Enum.valueOf(tClass, value))
                .orElse(null);
    }
}
