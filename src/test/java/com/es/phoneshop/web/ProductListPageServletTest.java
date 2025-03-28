package com.es.phoneshop.web;

import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.service.impl.RecentlyViewedProductsServiceImpl;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Deque;
import java.util.LinkedList;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ProductListPageServletTest {
    private static final String QUERY = "query";
    private static final String PRODUCT_DAO = "productDao";
    private static final String DESCRIPTION = "description";
    private static final String ASC = "asc";
    private static final String ERROR_MESSAGE = "Database error";
    private static final String SESSION_ATTRIBUTE = RecentlyViewedProductsServiceImpl.class.getName();
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private ArrayListProductDao productDao;

    private ProductListPageServlet servlet;

    @Before
    public void setup() throws ServletException {
        servlet = new ProductListPageServlet();
        servlet.init(servletConfig);

        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Test
    public void testDoGetSetsAttributesAndForwards() throws ServletException, IOException {
        Deque<Product> existingDeque = new LinkedList<>();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(SESSION_ATTRIBUTE)).thenReturn(existingDeque);

        servlet.doGet(request, response);

        verify(request, times(2)).setAttribute(anyString(), anyList());
        verify(requestDispatcher).forward(request, response);
    }

    @Test(expected = RuntimeException.class)
    public void testDoGetWhenDaoThrowsExceptionThrowsRuntimeException() throws ServletException, IOException {
        try (MockedStatic<ArrayListProductDao> mocked = mockStatic(ArrayListProductDao.class)) {
            mocked.when(ArrayListProductDao::getInstance).thenReturn(productDao);
            when(productDao.findProducts(QUERY,
                    SortField.valueOf(DESCRIPTION.toUpperCase()),
                    SortOrder.valueOf(ASC)))
                    .thenThrow(new RuntimeException(ERROR_MESSAGE));

            ProductListPageServlet servlet = new ProductListPageServlet();
            servlet.init(servletConfig);

            servlet.doGet(request, response);
        }
    }

    @Test
    public void testInitWhenProductDaoIsNullInitializesNewDao() throws Exception {
        ProductListPageServlet servlet = new ProductListPageServlet();
        servlet.init(servletConfig);

        Field field = ProductListPageServlet.class.getDeclaredField(PRODUCT_DAO);
        field.setAccessible(true);
        ArrayListProductDao productDao = (ArrayListProductDao) field.get(servlet);

        assertNotNull(productDao);
    }
}
