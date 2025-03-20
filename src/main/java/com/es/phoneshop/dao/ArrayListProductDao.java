package com.es.phoneshop.dao;

import com.es.phoneshop.comparators.ProductByDescriptionComparator;
import com.es.phoneshop.comparators.ProductByOrderComparator;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.exception.ProductNotFoundException;
import com.es.phoneshop.model.Product;
import com.es.phoneshop.utils.ReadWriteLockWrapper;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ArrayListProductDao implements ProductDao {
    private static final long INITIAL_PRODUCT_ID = 1;
    private static volatile ArrayListProductDao instance;
    private final List<Product> products;
    private final AtomicLong idGenerator;
    private final ReadWriteLockWrapper readWriteLock;
    private static final String REGEX = " ";

    private ArrayListProductDao() {
        products = new ArrayList<>();
        readWriteLock = new ReadWriteLockWrapper();
        idGenerator = new AtomicLong(INITIAL_PRODUCT_ID);
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
    public List<Product> findProducts(String query, SortField sortField, SortOrder sortOrder) {
        return readWriteLock.read(() -> products.stream()
                .filter(this::isProductAvailable)
                .filter(product -> productDescriptionMatchesQuery(product, query))
                .sorted(new ProductByDescriptionComparator(query))
                .sorted(new ProductByOrderComparator(sortField, sortOrder))
                .collect(Collectors.toList()));
    }

    private boolean productDescriptionMatchesQuery(Product product, String query) {
        return StringUtils.isBlank(query) || Arrays.stream(query.split(REGEX))
                .anyMatch(word -> product.getDescription().toLowerCase(Locale.ROOT)
                        .contains(word.toLowerCase(Locale.ROOT)));
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
}
