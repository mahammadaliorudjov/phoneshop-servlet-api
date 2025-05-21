package com.es.phoneshop.dao.impl;

import com.es.phoneshop.comparators.ProductByDescriptionComparator;
import com.es.phoneshop.comparators.ProductByOrderComparator;
import com.es.phoneshop.dao.AbstractDao;
import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.enums.SearchMethod;
import com.es.phoneshop.enums.SortField;
import com.es.phoneshop.enums.SortOrder;
import com.es.phoneshop.exception.ProductNotFoundException;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.predicates.ProductByMaxPricePredicate;
import com.es.phoneshop.predicates.ProductByMinPricePredicate;
import com.es.phoneshop.predicates.SearchByStrategyPredicate;
import com.es.phoneshop.utils.impl.ReadWriteLockWrapper;
import org.codehaus.plexus.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrayListProductDao extends AbstractDao<Product> implements ProductDao {
    private static volatile ArrayListProductDao instance;
    private final ReadWriteLockWrapper readWriteLock;
    private static final String REGEX = " ";
    private static final String STOCK_IS_NEGATIVE_EXCEPTION = "Stock cannot be negative";
    private static final String FIELD_IS_EMPTY_EXCEPTION = "Field cannot be empty";
    private static final String PRODUCT_VALIDATION_FAILED_EXCEPTION = "Product validation failed. ";
    private static final String PRODUCT_ID_NOT_FOUND_EXCEPTION = "Product with following id is not found: ";

    private ArrayListProductDao() {
        items = new ArrayList<>();
        readWriteLock = new ReadWriteLockWrapper();
        setValidator(this::validateProduct);
        setExceptionProvider(id ->
                new ProductNotFoundException(PRODUCT_ID_NOT_FOUND_EXCEPTION + id)
        );
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
    public List<Product> findProducts(String query, SortField sortField, SortOrder sortOrder) {
        return readWriteLock.read(() -> items.stream()
                .filter(this::isProductAvailable)
                .filter(product -> productDescriptionMatchesQuery(product, query))
                .sorted(new ProductByDescriptionComparator(query))
                .sorted(new ProductByOrderComparator(sortField, sortOrder))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Product> advancedSearch(String query, BigDecimal minPrice, BigDecimal maxPrice, SearchMethod searchMethod) {
        return readWriteLock.read(() -> findProducts(items)
                .filter(new ProductByMinPricePredicate(minPrice))
                .filter(new ProductByMaxPricePredicate(maxPrice))
                .filter(new SearchByStrategyPredicate(query, searchMethod))
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

    private void validateProduct(Product product) {
        Set<String> errors = new HashSet<>();

        validateNotEmpty(product.getDescription()).ifPresent(errors::add);
        validateNotEmpty(product.getCode()).ifPresent(errors::add);
        validateNotEmpty(product.getImageUrl()).ifPresent(errors::add);

        if (product.getStock() < 0) {
            errors.add(STOCK_IS_NEGATIVE_EXCEPTION);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(PRODUCT_VALIDATION_FAILED_EXCEPTION + String.join(", ", errors));
        }
    }

    private Optional<String> validateNotEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Optional.of(FIELD_IS_EMPTY_EXCEPTION);
        }
        return Optional.empty();
    }

    private Stream<Product> findProducts(List<Product> products) {
        return products.stream()
                .filter(this::isProductAvailable);
    }
}
