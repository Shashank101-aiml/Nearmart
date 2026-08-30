package com.buildit.dto.response;

import java.time.LocalDateTime;

public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Boolean available;
    private LocalDateTime createdAt;
    private Long vendorId;
    private String storeName;
    private Integer stockQuantity;

    public ProductResponse(Long id, String title, String description, Double price, Boolean available,
                            LocalDateTime createdAt, Long vendorId, String storeName, Integer stockQuantity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.available = available;
        this.createdAt = createdAt;
        this.vendorId = vendorId;
        this.storeName = storeName;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
