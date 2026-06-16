package com.sme.erp.product.dto;

public class ProductStatsDTO {
    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;
    private long productsWithoutImage;

    public ProductStatsDTO(long totalProducts, long activeProducts, long inactiveProducts, long productsWithoutImage) {
        this.totalProducts = totalProducts;
        this.activeProducts = activeProducts;
        this.inactiveProducts = inactiveProducts;
        this.productsWithoutImage = productsWithoutImage;
    }

    public long getTotalProducts() { return totalProducts; }
    public long getActiveProducts() { return activeProducts; }
    public long getInactiveProducts() { return inactiveProducts; }
    public long getProductsWithoutImage() { return productsWithoutImage; }
}
