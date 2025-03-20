package com.es.phoneshop.web;

import com.es.phoneshop.dao.ArrayListProductDao;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
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
    private static final String QUERY = "query";
    private static final String SORT_FIELD = "sort";
    private static final String SORT_ORDER = "order";
    private static final String PRODUCT_DAO = "productDao";
    private static final String DESCRIPTION = "description";
    private static final String ASC = "asc";
    private static final String ERROR_MESSAGE = "Database error";
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
