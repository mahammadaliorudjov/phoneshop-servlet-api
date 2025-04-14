package com.es.phoneshop.model.product;

import com.es.phoneshop.model.Entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public class Product extends Entity implements Serializable {
    public Product(String code, String description, BigDecimal price, Currency currency, int stock, String imageUrl, List<ProductPriceHistory> productPriceHistoryList) {
        this.code = code;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.productPriceHistoryList = productPriceHistoryList;
    }

    private Long id;
    private String code;
    private String description;
    /**
     * null means there is no price because the product is outdated or new
     */
    private BigDecimal price;
    /**
     * can be null if the price is null
     */
    private Currency currency;
    private int stock;
    private String imageUrl;
    private List<ProductPriceHistory> productPriceHistoryList;

    public Product() {
    }

    public Product(Long id, String code, String description, BigDecimal price, Currency currency, int stock, String imageUrl) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public Product(String code, String description, BigDecimal price, Currency currency, int stock, String imageUrl) {
        this.code = code;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<ProductPriceHistory> getProductPriceHistoryList() {
        return productPriceHistoryList;
    }

    public void setProductPriceHistoryList(List<ProductPriceHistory> productPriceHistoryList) {
        this.productPriceHistoryList = productPriceHistoryList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!obj.getClass().equals(Product.class)) {
            return false;
        }
        Product product = (Product) obj;
        return getId().equals(product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
