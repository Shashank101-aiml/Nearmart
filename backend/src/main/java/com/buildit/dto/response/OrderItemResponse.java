package com.buildit.dto.response;

public class OrderItemResponse {
    private Long productId;
    private String productTitle;
    private Double unitPrice;
    private Integer quantity;
    private Double lineTotal;

    public OrderItemResponse(Long productId, String productTitle, Double unitPrice, Integer quantity,
                              Double lineTotal) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getLineTotal() { return lineTotal; }
    public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
}
