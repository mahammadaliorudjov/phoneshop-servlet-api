package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
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
import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ProductListPageServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
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
    public void testDoGetSetsProductsAttributeAndForwards() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).setAttribute(anyString(), anyList());
        verify(requestDispatcher).forward(request, response);
    }

    @Test(expected = RuntimeException.class)
    public void testDoGetWhenDaoThrowsExceptionThrowsRuntimeException() throws ServletException, IOException {
        try (MockedStatic<ArrayListProductDao> mocked = mockStatic(ArrayListProductDao.class)) {
            mocked.when(ArrayListProductDao::getInstance).thenReturn(productDao);
            when(productDao.findProducts()).thenThrow(new RuntimeException("Database error"));

            ProductListPageServlet servlet = new ProductListPageServlet();
            servlet.init(servletConfig);

            servlet.doGet(request, response);
        }
    }

    @Test
    public void testInitWhenProductDaoIsNullInitializesNewDao() throws Exception {
        ProductListPageServlet servlet = new ProductListPageServlet();
        servlet.init(servletConfig);

        Field field = ProductListPageServlet.class.getDeclaredField("productDao");
        field.setAccessible(true);
        ArrayListProductDao productDao = (ArrayListProductDao) field.get(servlet);

        assertNotNull(productDao);
    }
}
