package com.es.phoneshop.web;

import com.es.phoneshop.dao.ArrayListProductDao;
import com.es.phoneshop.dao.ProductDao;
import com.es.phoneshop.model.Product;
import com.es.phoneshop.model.ProductPriceHistory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class ProductDemodataServletContextListener implements ServletContextListener {
    private ProductDao productDao;
    private static final String USD = "USD";

    public ProductDemodataServletContextListener() {
        this.productDao = ArrayListProductDao.getInstance();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        boolean demodata = Boolean.parseBoolean(sce.getServletContext().getInitParameter("demodata"));
        if (demodata) {
            saveSampleProducts(productDao);
        }
    }

    private void saveSampleProducts(ProductDao productDao) {
        Currency usd = Currency.getInstance(USD);
        productDao.save(new Product("sgs", "Samsung Galaxy S", new BigDecimal(100), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S.jpg", createProductPriceHistory("Samsung Galaxy S", new BigDecimal(100))));
        productDao.save(new Product("sgs2", "Samsung Galaxy S II", new BigDecimal(200), usd, 0, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20II.jpg", createProductPriceHistory("Samsung Galaxy S II", new BigDecimal(200))));
        productDao.save(new Product("sgs3", "Samsung Galaxy S III", new BigDecimal(300), usd, 5, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Samsung/Samsung%20Galaxy%20S%20III.jpg", createProductPriceHistory("Samsung Galaxy S III", new BigDecimal(300))));
        productDao.save(new Product("iphone", "Apple iPhone", new BigDecimal(200), usd, 10, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone.jpg", createProductPriceHistory("Apple iPhone", new BigDecimal(200))));
        productDao.save(new Product("iphone6", "Apple iPhone 6", new BigDecimal(1000), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Apple/Apple%20iPhone%206.jpg", createProductPriceHistory("Apple iPhone 6", new BigDecimal(1000))));
        productDao.save(new Product("htces4g", "HTC EVO Shift 4G", new BigDecimal(320), usd, 3, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/HTC/HTC%20EVO%20Shift%204G.jpg", createProductPriceHistory("HTC EVO Shift 4G", new BigDecimal(320))));
        productDao.save(new Product("sec901", "Sony Ericsson C901", new BigDecimal(420), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Ericsson%20C901.jpg", createProductPriceHistory("Sony Ericsson C901", new BigDecimal(420))));
        productDao.save(new Product("xperiaxz", "Sony Xperia XZ", new BigDecimal(120), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Sony/Sony%20Xperia%20XZ.jpg", createProductPriceHistory("Sony Xperia XZ", new BigDecimal(120))));
        productDao.save(new Product("nokia3310", "Nokia 3310", new BigDecimal(70), usd, 100, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Nokia/Nokia%203310.jpg", createProductPriceHistory("Nokia 3310", new BigDecimal(70))));
        productDao.save(new Product("palmp", "Palm Pixi", new BigDecimal(170), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Palm/Palm%20Pixi.jpg", createProductPriceHistory("Palm Pixi", new BigDecimal(170))));
        productDao.save(new Product("simc56", "Siemens C56", new BigDecimal(70), usd, 20, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20C56.jpg", createProductPriceHistory("Siemens C56", new BigDecimal(70))));
        productDao.save(new Product("simc61", "Siemens C61", new BigDecimal(80), usd, 30, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20C61.jpg", createProductPriceHistory("Siemens C61", new BigDecimal(80))));
        productDao.save(new Product("simsxg75", "Siemens SXG75", new BigDecimal(150), usd, 40, "https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/manufacturer/Siemens/Siemens%20SXG75.jpg", createProductPriceHistory("Siemens SXG75", new BigDecimal(150))));

    }

    public List<ProductPriceHistory> createProductPriceHistory(String description, BigDecimal price) {
        Currency usd = Currency.getInstance(USD);
        List<ProductPriceHistory> productPriceHistoryList = new ArrayList<>();
        productPriceHistoryList.add(new ProductPriceHistory(description, LocalDate.now(), usd, price));
        productPriceHistoryList.add(new ProductPriceHistory(description, LocalDate.of(2025, 1, 1), usd, new BigDecimal(200)));
        productPriceHistoryList.add(new ProductPriceHistory(description, LocalDate.of(2024, 1, 1), usd, new BigDecimal(300)));

        return productPriceHistoryList;
    }
}
