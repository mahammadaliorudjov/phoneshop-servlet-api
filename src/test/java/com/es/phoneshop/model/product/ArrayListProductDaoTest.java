package com.es.phoneshop.model.product;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Currency;
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


public class ArrayListProductDaoTest {
    private ArrayListProductDao productDao;
    private Currency usd;
    private static final long NON_EXISTENT_PRODUCT_ID = 10000L;
    private static final long NEW_PRODUCT_ID = 14L;
    private static final long INITIAL_PRODUCTS_COUNT = 12;
    private static final String PRODUCT_DESCRIPTION = "Expert Soft";
    private static final String USD = "USD";
    private static final int THREAD_COUNT = 10;
    private static final int EXPECTED_INSTANCE_COUNT = 1;

    @Before
    public void setup() {
        productDao = ArrayListProductDao.getInstance();
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
        Product existingProduct = productDao.findProducts().get(index);

        Product result = productDao.getProduct(existingProduct.getId());

        assertEquals(existingProduct.getId(), result.getId());
    }

    @Test(expected = ProductNotFoundException.class)
    public void testGetProductNonExistingIdThrowsException() {
        productDao.getProduct(NON_EXISTENT_PRODUCT_ID);
    }

    @Test
    public void testFindProductsFiltersByPriceAndStock() {
        List<Product> filteredProducts = productDao.findProducts();

        assertTrue(filteredProducts.stream().noneMatch(p -> p.getStock() <= 0));
        assertTrue(filteredProducts.stream().allMatch(p -> p.getPrice() != null));
    }

    @Test
    public void testFindProductsExcludesProductsWithNullPrice() {
        Product nullPriceProduct = new Product("op9rt", "OnePlus 9RT", null, usd, 10, "image.jpg");
        productDao.save(nullPriceProduct);

        List<Product> result = productDao.findProducts();

        assertFalse(result.contains(nullPriceProduct));
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
        Product existingProduct = productDao.findProducts().get(0);
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
        Product result = productDao.getProduct(existingProduct.getId());

        assertEquals(PRODUCT_DESCRIPTION, result.getDescription());
    }

    @Test(expected = ProductNotFoundException.class)
    public void testSaveNonExistingProductThrowsException() {
        Product product = new Product(NON_EXISTENT_PRODUCT_ID, "op9rt", "OnePlus 9RT", new BigDecimal(500), usd, 500, "image.jpg");

        productDao.save(product);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveExistingProductInvalidDescription() {
        Product existingProduct = productDao.findProducts().get(0);
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
        Product existingProduct = productDao.findProducts().get(0);
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
        Product existingProduct = productDao.findProducts().get(0);
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
        Product existingProduct = productDao.findProducts().get(0);
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
        Product existingProduct = productDao.findProducts().get(0);
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
        Product productToDelete = productDao.findProducts().get(0);

        productDao.delete(productToDelete.getId());

        assertFalse(productDao.findProducts().contains(productToDelete));
    }

    @Test
    public void testSaveSampleProductsInitializesCorrectly() {
        assertEquals(INITIAL_PRODUCTS_COUNT, productDao.findProducts().size());
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
}
