package com.es.phoneshop.web;

import com.es.phoneshop.dao.OrderDao;
import com.es.phoneshop.dao.impl.ArrayListOrderDao;
import com.es.phoneshop.model.order.Order;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class OrderOverviewPageServlet extends HttpServlet {
    private static final String ORDER = "order";
    private static final String JSP_PATH = "/WEB-INF/pages/overview.jsp";
    private static final int PATH_ID_START_INDEX = 1;
    private OrderDao orderDao;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        orderDao = ArrayListOrderDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String secureId = request.getPathInfo().substring(PATH_ID_START_INDEX);
        Order order = orderDao.getOrderBySecureId(secureId);
        request.setAttribute(ORDER, order);
        request.getRequestDispatcher(JSP_PATH).forward(request, response);
    }
}
