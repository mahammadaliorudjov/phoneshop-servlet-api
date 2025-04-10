package com.es.phoneshop.web;

import com.es.phoneshop.dao.impl.ArrayListOrderDao;
import com.es.phoneshop.model.order.Order;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderOverviewPageServletTest {
    private static final String ORDER = "order";
    private static final String JSP_PATH = "/WEB-INF/pages/overview.jsp";
    private static final String SECURE_ID = "12345";
    private static final String PATH_INFO = "/" + SECURE_ID;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private ArrayListOrderDao orderDao;
    @Mock
    private Order order;

    private OrderOverviewPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<ArrayListOrderDao> orderDaoMocked = mockStatic(ArrayListOrderDao.class)) {
            orderDaoMocked.when(ArrayListOrderDao::getInstance).thenReturn(orderDao);
            servlet = new OrderOverviewPageServlet();
            servlet.init(servletConfig);
        }

        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(request.getRequestDispatcher(JSP_PATH)).thenReturn(requestDispatcher);
        when(orderDao.getOrderBySecureId(SECURE_ID)).thenReturn(order);
    }

    @Test
    public void testDoGetWithValidSecureId() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).getPathInfo();
        verify(orderDao).getOrderBySecureId(SECURE_ID);
        verify(request).setAttribute(ORDER, order);
        verify(request).getRequestDispatcher(JSP_PATH);
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    public void testDoGetWithNullOrder() throws ServletException, IOException {
        when(orderDao.getOrderBySecureId(SECURE_ID)).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).getPathInfo();
        verify(orderDao).getOrderBySecureId(SECURE_ID);
        verify(request).setAttribute(ORDER, null);
        verify(request).getRequestDispatcher(JSP_PATH);
        verify(requestDispatcher).forward(request, response);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDoGetWithEmptyPathInfo() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("");

        servlet.doGet(request, response);
    }
}
