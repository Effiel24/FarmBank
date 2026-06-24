package ru.agrobank.app.repository;

import ru.agrobank.app.model.InsuranceProduct;
import java.util.*;

public class ProductRepository {
    private final List<InsuranceProduct> products = new ArrayList<>();

    public ProductRepository() {
        // Доступные страховые продукты
        products.add(new InsuranceProduct(1, "Защита от града", 1500.0, "Страхование посевов от града и ливней"));
        products.add(new InsuranceProduct(2, "Страхование урожая", 2000.0, "Полная комплексная защита на сезон"));
        products.add(new InsuranceProduct(3, "Экспресс-страховка", 1200.0, "Базовый тариф с минимальным покрытием"));
    }

    public List<InsuranceProduct> getAllProducts() {
        return products;
    }

    public InsuranceProduct findById(int id) {
        for (InsuranceProduct p : products) {
            if (p.getProductId() == id) return p;
        }
        return null;
    }
}