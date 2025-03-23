package com.es.phoneshop.web;

import com.es.phoneshop.dao.impl.ArrayListProductDao;
import com.es.phoneshop.exception.ProductNotFoundException;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductPriceHistory;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductPriceHistoryPageServletTest {
    private static final long PRODUCT_ID = 1L;
    private static final String PATH_INFO = "/1";
    private static final String INVALID_PATH_INFO = "/100";
    private static final long NON_EXISTING_PRODUCT_ID = 100L;
    private ProductPriceHistoryPageServlet servlet;
    @Mock
    private List<ProductPriceHistory> priceHistoryList;
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
    @Mock
    Product product;

    @Before
    public void setup() throws ServletException {
        try (MockedStatic<ArrayListProductDao> mocked = mockStatic(ArrayListProductDao.class)) {
            mocked.when(ArrayListProductDao::getInstance).thenReturn(productDao);

            servlet = new ProductPriceHistoryPageServlet();
            servlet.init(servletConfig);
        }

        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(productDao.getProduct(PRODUCT_ID)).thenReturn(product);
        when(productDao.getProduct(PRODUCT_ID).getProductPriceHistoryList()).thenReturn(priceHistoryList);
    }

    @Test
    public void testDoGetExistingPriceHistoryList() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).getPathInfo();
        verify(request).setAttribute(any(), any());
        verify(requestDispatcher).forward(request, response);
    }

    @Test(expected = ProductNotFoundException.class)
    public void testDoGetNonExistingProductThrowsException() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(INVALID_PATH_INFO);
        when(productDao.getProduct(NON_EXISTING_PRODUCT_ID)).thenReturn(product);
        when(productDao.getProduct(NON_EXISTING_PRODUCT_ID).getProductPriceHistoryList())
                .thenThrow(new ProductNotFoundException("Product not found"));

        servlet.doGet(request, response);
    }
}
