package com.es.phoneshop.model.product;

import com.es.phoneshop.utils.ReadWriteLockWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ArrayListProductDao implements ProductDao {
    private static volatile ArrayListProductDao instance;
    private final List<Product> products;
    private final AtomicLong idGenerator;
    private final ReadWriteLockWrapper readWriteLock;
    private static final long INITIAL_PRODUCT_ID = 1;

    private ArrayListProductDao() {
        products = new ArrayList<>();
        readWriteLock = new ReadWriteLockWrapper();
        idGenerator = new AtomicLong(INITIAL_PRODUCT_ID);
        saveSampleProducts();
    }

    public static ArrayListProductDao getInstance() {
        if (instance == null) {
            synchronized (ArrayListProductDao.class) {
                if (instance == null) {
                    instance = new ArrayListProductDao();
                }
            }
        }
        return instance;
    }

    @Override
    public Product getProduct(Long id) {
        return readWriteLock.read(() -> products.stream()
                .filter(product -> id.equals(product.getId()))
                .findAny()
                .orElseThrow(() -> new ProductNotFoundException("Product with id" + id + " not found")));
    }

    @Override
    public List<Product> findProducts() {
        return readWriteLock.read(() -> products.stream()
                .filter(this::isProductAvailable)
                .collect(Collectors.toList()));
    }

    private boolean isProductAvailable(Product product) {
        return product.getPrice() != null
                && product.getStock() > 0;
    }

    @Override
    public void save(Product product) {
        readWriteLock.write(() -> {
            validateProduct(product);
            if (product.getId() == null) {
                product.setId(idGenerator.getAndIncrement());
                products.add(product);
            } else {
                Product existingProduct = getProduct(product.getId());
                int index = products.indexOf(existingProduct);
                products.set(index, product);
            }
        });
    }

    private void validateProduct(Product product) {
        Set<String> errors = new HashSet<>();

        validateNotEmpty(product.getDescription()).ifPresent(errors::add);
        validateNotEmpty(product.getCode()).ifPresent(errors::add);
        validateNotEmpty(product.getImageUrl()).ifPresent(errors::add);

        if (product.getStock() < 0) {
            errors.add("Stock cannot be negative");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Product validation failed: " + String.join(", ", errors));
        }
    }

    private Optional<String> validateNotEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Optional.of("Field cannot be empty");
        }
        return Optional.empty();
    }

    @Override
    public void delete(Long id) {
        readWriteLock.write(() -> {
            products.removeIf(product -> id.equals(product.getId()));
        });
    }

    private void saveSampleProducts() {
        Currency usd = Currency.getInstance("USD");
        save(new Product("sgs", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg"));
        save(new Product("sgs2", "Samsung Galaxy S II", new BigDecimal(200), usd, 0, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20II.jpg"));
        save(new Product("sgs3", "Samsung Galaxy S III", new BigDecimal(300), usd, 5, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20III.jpg"));
        save(new Product("iphone", "Apple iPhone", new BigDecimal(200), usd, 10, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone.jpg"));
        save(new Product("iphone6", "Apple iPhone 6", new BigDecimal(1000), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone%206.jpg"));
        save(new Product("htces4g", "HTC EVO Shift 4G", new BigDecimal(320), usd, 3, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/HTC/HTC%20EVO%20Shift%204G.jpg"));
        save(new Product("sec901", "Sony Ericsson C901", new BigDecimal(420), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Ericsson%20C901.jpg"));
        save(new Product("xperiaxz", "Sony Xperia XZ", new BigDecimal(120), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Xperia%20XZ.jpg"));
        save(new Product("nokia3310", "Nokia 3310", new BigDecimal(70), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Nokia/Nokia%203310.jpg"));
        save(new Product("palmp", "Palm Pixi", new BigDecimal(170), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Palm/Palm%20Pixi.jpg"));
        save(new Product("simc56", "Siemens C56", new BigDecimal(70), usd, 20, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20C56.jpg"));
        save(new Product("simc61", "Siemens C61", new BigDecimal(80), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20C61.jpg"));
        save(new Product("simsxg75", "Siemens SXG75", new BigDecimal(150), usd, 40, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20SXG75.jpg"));
    }
}
