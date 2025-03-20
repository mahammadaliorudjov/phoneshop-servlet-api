package com.es.phoneshop.web;

import com.es.phoneshop.dao.ArrayListProductDao;
import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.model.ProductPriceHistory;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ProductPriceHistoryPageServlet extends HttpServlet {
    private static final int PATH_ID_START_INDEX = 1;
    private ProductDao productDao;
    private static final String PRICE_HISTORY = "priceHistory";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        productDao = ArrayListProductDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long id = Long.parseLong(request.getPathInfo().substring(PATH_ID_START_INDEX));
        List<ProductPriceHistory> priceHistoryList = productDao.getProduct(id).getProductPriceHistoryList();
        request.setAttribute("priceHistory", priceHistoryList);
        request.getRequestDispatcher("/WEB-INF/pages/productPriceHistory.jsp").forward(request, response);
    }
}
