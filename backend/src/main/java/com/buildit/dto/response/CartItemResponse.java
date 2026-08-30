package com.buildit.dto.response;

public class CartItemResponse {
    private Long productId;
    private String productTitle;
    private Double price;
    private Integer quantity;
    private Double lineTotal;
    private Integer availableStock;

    public CartItemResponse(Long productId, String productTitle, Double price, Integer quantity,
                             Double lineTotal, Integer availableStock) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.price = price;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.availableStock = availableStock;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getLineTotal() { return lineTotal; }
    public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
}
