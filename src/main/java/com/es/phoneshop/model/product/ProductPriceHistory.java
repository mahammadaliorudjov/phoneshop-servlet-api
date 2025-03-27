package com.es.phoneshop.model.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public class ProductPriceHistory implements Serializable {
    private String description;
    private LocalDate date;
    private Currency currency;
    private BigDecimal price;

    public ProductPriceHistory(String description, LocalDate date, Currency currency, BigDecimal price) {
        this.description = description;
        this.date = date;
        this.currency = currency;
        this.price = price;
    }

    public ProductPriceHistory() {
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
