package com.es.phoneshop.web;

import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.enums.SearchMethod;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.RecentlyViewedProductsService;
import com.es.phoneshop.service.impl.RecentlyViewedProductsServiceImpl;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.maven.shared.utils.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedSearchPageServlet extends HttpServlet {
    private ProductDao productDao;
    private RecentlyViewedProductsService recentlyViewedProductService;
    private static final String ADVANCED_SEARCH_JSP = "/WEB-INF/pages/advancedSearch.jsp";
    private static final String RECENTLY_VIEWED_PRODUCTS = "recentlyViewedProducts";
    private static final String MIN_PRICE = "minPrice";
    private static final String MAX_PRICE = "maxPrice";
    private static final String QUERY = "query";
    private static final String SEARCH_METHOD = "searchMethod";
    private static final String PRODUCTS = "products";
    private static final String ERRORS = "errors";
    private static final String SEARCH_METHODS = "searchMethods";
    private static final String NOT_A_NUMBER_ERROR = "Not a number";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        productDao = ArrayListProductDao.getInstance();
        recentlyViewedProductService = RecentlyViewedProductsServiceImpl.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();
        String search = request.getParameter(QUERY);
        SearchMethod searchMethod = getSearchMethod(request);
        BigDecimal minPrice = parseBigDecimal(request, MIN_PRICE, errors);
        BigDecimal maxPrice = parseBigDecimal(request, MAX_PRICE, errors);
        if (search != null && errors.isEmpty()) {
            List<Product> products = productDao.advancedSearch(search, minPrice, maxPrice, searchMethod);
            request.setAttribute(PRODUCTS, products);
        } else {
            request.setAttribute(ERRORS, errors);
        }
        request.setAttribute(SEARCH_METHODS, Arrays.asList(SearchMethod.values()));
        request.setAttribute(RECENTLY_VIEWED_PRODUCTS,
                recentlyViewedProductService.getViewedProducts(request));
        request.getRequestDispatcher(ADVANCED_SEARCH_JSP).forward(request, response);
    }

    private BigDecimal parseBigDecimal(HttpServletRequest request, String priceParameterName, Map<String, String> errors) {
        String bigDecimalString = request.getParameter(priceParameterName);
        if (StringUtils.isBlank(bigDecimalString)) {
            return null;
        }
        try {
            return new BigDecimal(bigDecimalString);
        } catch (NumberFormatException e) {
            errors.put(priceParameterName, NOT_A_NUMBER_ERROR);
            return null;
        }
    }

    private SearchMethod getSearchMethod(HttpServletRequest request) {
        String searchMethodString = request.getParameter(SEARCH_METHOD);
        if (searchMethodString == null) {
            return SearchMethod.ANY_WORD;
        } else {
            return SearchMethod.valueOf(searchMethodString);
        }
    }
}
