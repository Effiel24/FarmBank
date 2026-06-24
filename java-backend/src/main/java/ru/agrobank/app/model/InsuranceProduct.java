package ru.agrobank.app.model;

public class InsuranceProduct {
    private int productId;
    private String name;
    private double ratePerHa;
    private String description;

    public InsuranceProduct(int productId, String name, double ratePerHa, String description) {
        this.productId = productId;
        this.name = name;
        this.ratePerHa = ratePerHa;
        this.description = description;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public double getRatePerHa() { return ratePerHa; }
    public String getDescription() { return description; }
}