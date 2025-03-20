package com.es.phoneshop.web;

import com.es.phoneshop.dao.ArrayListProductDao;
import com.es.phoneshop.exception.ProductNotFoundException;
import com.es.phoneshop.model.Product;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductDetailsPageServletTest {
    private static final long PRODUCT_ID = 1L;
    private static final String PATH_INFO = "/1";
    private static final String INVALID_PATH_INFO = "/100";
    private static final long NON_EXISTING_PRODUCT_ID = 100L;
    private ProductDetailsPageServlet servlet;
    @Mock
    private Product product;
    @Mock
    private ArrayListProductDao productDao;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private RequestDispatcher requestDispatcher;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<ArrayListProductDao> mocked = mockStatic(ArrayListProductDao.class)) {
            mocked.when(ArrayListProductDao::getInstance).thenReturn(productDao);

            servlet = new ProductDetailsPageServlet();
            servlet.init(servletConfig);
        }

        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(productDao.getProduct(PRODUCT_ID)).thenReturn(product);
    }

    @Test
    public void testDoGetExistingProduct() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).getPathInfo();
        verify(request).setAttribute(any(), any());
        verify(requestDispatcher).forward(request, response);
    }

    @Test(expected = ProductNotFoundException.class)
    public void testDoGetNonExistingProductThrowsException() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(INVALID_PATH_INFO);
        when(productDao.getProduct(NON_EXISTING_PRODUCT_ID))
                .thenThrow(new ProductNotFoundException("Product not found"));

        servlet.doGet(request, response);
    }
}
