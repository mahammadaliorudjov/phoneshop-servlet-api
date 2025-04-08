package com.es.phoneshop.dao.impl;

import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.exception.ProductNotFoundException;
import com.es.phoneshop.model.product.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ArrayListProductDaoTest {
    private ArrayListProductDao productDao;
    private Currency usd;
    private static final long NON_EXISTENT_PRODUCT_ID = 10000L;
    private static final long NEW_PRODUCT_ID = 9L;
    private static final long SAMSUNG_PRODUCTS_COUNT = 2;
    private static final String PRODUCT_DESCRIPTION = "Expert Soft";
    private static final String USD = "USD";
    private static final int THREAD_COUNT = 10;
    private static final int EXPECTED_INSTANCE_COUNT = 1;
    private static final int INITIAL_PRODUCTS_COUNT = 7;
    private static final String QUERY = "Samsung";
    private static final String GALAXY = "galaxy";

    @Before
    public void setup() {
        productDao = ArrayListProductDao.getInstance();
        productDao.save(new Product("sgs", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg"));
        productDao.save(new Product("sgs2", "Samsung Galaxy S II", new BigDecimal(200), usd, 0, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20II.jpg"));
        productDao.save(new Product("sgs3", "Samsung Galaxy S III", new BigDecimal(300), usd, 5, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20III.jpg"));
        productDao.save(new Product("iphone", "Apple iPhone", new BigDecimal(200), usd, 10, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone.jpg"));
        productDao.save(new Product("iphone6", "Apple iPhone 6", new BigDecimal(1000), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone%206.jpg"));
        productDao.save(new Product("htces4g", "HTC EVO Shift 4G", new BigDecimal(320), usd, 3, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/HTC/HTC%20EVO%20Shift%204G.jpg"));
        productDao.save(new Product("sec901", "Sony Ericsson C901", new BigDecimal(420), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Ericsson%20C901.jpg"));
        productDao.save(new Product("xperiaxz", "Sony Xperia XZ", new BigDecimal(120), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Xperia%20XZ.jpg"));
        usd = Currency.getInstance(USD);
        resetSingleton();
    }

    private void resetSingleton() {
        try {
            Field instanceField = ArrayListProductDao.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to reset singleton", ex);
        }
    }

    @Test
    public void testGetProductExistingIdReturnsProduct() {
        int index = 0;
        Product existingProduct = productDao.findProducts(QUERY, null, null).get(index);

        Product result = productDao.get(existingProduct.getId());

        assertEquals(existingProduct.getId(), result.getId());
    }

    @Test(expected = ProductNotFoundException.class)
    public void testGetProductNonExistingIdThrowsException() {
        productDao.get(NON_EXISTENT_PRODUCT_ID);
    }

    @Test
    public void testFindProductsFiltersByPriceAndStock() {
        List<Product> filteredProducts = productDao.findProducts(QUERY, null, null);

        assertTrue(filteredProducts.stream().noneMatch(p -> p.getStock() <= 0));
        assertTrue(filteredProducts.stream().allMatch(p -> p.getPrice() != null));
    }

    @Test
    public void testFindProductsEmptyQueryReturnsAllProducts() {
        List<Product> products = productDao.findProducts("", null, null);

        assertEquals(INITIAL_PRODUCTS_COUNT, products.size());
    }

    @Test
    public void testFindProductsExcludesProductsWithNullPrice() {
        Product nullPriceProduct = new Product("op9rt", "OnePlus 9RT", null, usd, 10, "image.jpg");
        productDao.save(nullPriceProduct);

        List<Product> result = productDao.findProducts("OnePlus", null, null);

        assertFalse(result.contains(nullPriceProduct));
    }

    @Test
    public void testFindProducts_FilterByDescriptionQuery() {
        List<Product> result = productDao.findProducts(GALAXY, null, null);

        assertTrue(result.stream().allMatch(p -> p.getDescription().toLowerCase().contains(GALAXY)));
    }

    @Test
    public void testFindProducts_SortByPriceAscending() {
        List<Product> result = productDao.findProducts("", SortField.PRICE, SortOrder.ASC);

        assertTrue(isSorted(result, Comparator.comparing(Product::getPrice)));
    }

    @Test
    public void testFindProducts_SortByPriceDescending() {
        List<Product> result = productDao.findProducts("", SortField.PRICE, SortOrder.DESC);

        assertTrue(isSorted(result, (p1, p2) ->
                p2.getPrice().compareTo(p1.getPrice())));
    }

    @Test
    public void testFindProducts_SortByDescriptionAscending() {
        List<Product> result = productDao.findProducts("", SortField.DESCRIPTION, SortOrder.ASC);

        assertTrue(isSorted(result, Comparator.comparing(Product::getDescription)));
    }

    @Test
    public void testFindProducts_SortByDescriptionDescending() {
        List<Product> result = productDao.findProducts("", SortField.DESCRIPTION, SortOrder.DESC);

        assertTrue(isSorted(result, (p1, p2) ->
                p2.getDescription().compareTo(p1.getDescription())));
    }

    @Test
    public void testFindProducts_CombinedSorting() {
        List<Product> result = productDao.findProducts(QUERY, SortField.PRICE, SortOrder.DESC);

        assertTrue(result.get(0).getPrice().compareTo(result.get(1).getPrice()) > 0);
    }

    @Test
    public void testSaveNewProductGeneratesId() {
        Product newProduct = new Product("op9rt", "OnePlus 9RT", new BigDecimal(500), usd, 500, "image.jpg");

        productDao.save(newProduct);

        assertNotNull(newProduct.getId());
        assertEquals(NEW_PRODUCT_ID, (long) newProduct.getId());
    }

    @Test
    public void testSaveExistingProductUpdatesProduct() {
        Product existingProduct = productDao.findProducts(QUERY, null, null).get(0);
        Product updatedProduct = new Product(
                existingProduct.getId(),
                existingProduct.getCode(),
                "Expert Soft",
                existingProduct.getPrice(),
                existingProduct.getCurrency(),
                existingProduct.getStock(),
                existingProduct.getImageUrl()
        );

        productDao.save(updatedProduct);
        Product result = productDao.get(existingProduct.getId());

        assertEquals(PRODUCT_DESCRIPTION, result.getDescription());
    }

    @Test(expected = ProductNotFoundException.class)
    public void testSaveNonExistingProductThrowsException() {
        Product product = new Product(NON_EXISTENT_PRODUCT_ID, "op9rt", "OnePlus 9RT", new BigDecimal(500), usd, 500, "image.jpg");

        productDao.save(product);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveExistingProductInvalidDescription() {
        Product existingProduct = productDao.findProducts(QUERY, null, null).get(0);
        Product updatedProduct = new Product(
                existingProduct.getId(),
                existingProduct.getCode(),
                "",
                existingProduct.getPrice(),
                existingProduct.getCurrency(),
                existingProduct.getStock(),
                existingProduct.getImageUrl()
        );

        productDao.save(updatedProduct);
    }

    @Test
    public void testSaveExistingProductNullPrice() {
        Product existingProduct = productDao.findProducts(QUERY, null, null).get(0);
        Product updatedProduct = new Product(
                existingProduct.getId(),
                existingProduct.getCode(),
                existingProduct.getDescription(),
                null,
                existingProduct.getCurrency(),
                existingProduct.getStock(),
                existingProduct.getImageUrl()
        );

        productDao.save(updatedProduct);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveExistingProductNegativeStock() {
        Product existingProduct = productDao.findProducts(QUERY, null, null).get(0);
        Product updatedProduct = new Product(
                existingProduct.getId(),
                existingProduct.getCode(),
                existingProduct.getDescription(),
                existingProduct.getPrice(),
                existingProduct.getCurrency(),
                -5,
                existingProduct.getImageUrl()
        );

        productDao.save(updatedProduct);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveExistingProductEmptyImageUrl() {
        Product existingProduct = productDao.findProducts(QUERY, null, null).get(0);
        Product updatedProduct = new Product(
                existingProduct.getId(),
                existingProduct.getCode(),
                existingProduct.getDescription(),
                existingProduct.getPrice(),
                existingProduct.getCurrency(),
                existingProduct.getStock(),
                ""
        );

        productDao.save(updatedProduct);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveExistingProductEmptyCode() {
        Product existingProduct = productDao.findProducts(QUERY, null, null).get(0);
        Product updatedProduct = new Product(
                existingProduct.getId(),
                "",
                existingProduct.getDescription(),
                existingProduct.getPrice(),
                existingProduct.getCurrency(),
                existingProduct.getStock(),
                existingProduct.getImageUrl()
        );

        productDao.save(updatedProduct);
    }


    @Test
    public void testDeleteExistingProductRemovesFromList() {
        Product productToDelete = productDao.findProducts(QUERY, null, null).get(0);

        productDao.delete(productToDelete.getId());

        assertFalse(productDao.findProducts(QUERY, null, null).contains(productToDelete));
    }

    @Test
    public void testSaveSampleProductsInitializesCorrectly() {
        assertEquals(SAMSUNG_PRODUCTS_COUNT, productDao.findProducts(QUERY, null, null).size());
    }

    @Test
    public void testSingletonConcurrentAccess() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Set<ArrayListProductDao> instances = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                instances.add(ArrayListProductDao.getInstance());
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();

        assertEquals(EXPECTED_INSTANCE_COUNT, instances.size());
    }

    private boolean isSorted(List<Product> products, Comparator<Product> comparator) {
        Iterator<Product> iterator = products.iterator();
        Product prev = iterator.next();
        while (iterator.hasNext()) {
            Product curr = iterator.next();
            if (comparator.compare(prev, curr) > 0) {
                return false;
            }
            prev = curr;
        }
        return true;
    }
}
