package com.buildit.dto.response;

public class AdminOrderItemResponse {
    private Long productId;
    private String productTitle;
    private Double unitPrice;
    private Integer quantity;
    private Double lineTotal;
    private Long vendorId;
    private String vendorStoreName;
    private String fulfillmentStatus;

    public AdminOrderItemResponse(Long productId, String productTitle, Double unitPrice, Integer quantity,
                                   Double lineTotal, Long vendorId, String vendorStoreName,
                                   String fulfillmentStatus) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.vendorId = vendorId;
        this.vendorStoreName = vendorStoreName;
        this.fulfillmentStatus = fulfillmentStatus;
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
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getVendorStoreName() { return vendorStoreName; }
    public void setVendorStoreName(String vendorStoreName) { this.vendorStoreName = vendorStoreName; }
    public String getFulfillmentStatus() { return fulfillmentStatus; }
    public void setFulfillmentStatus(String fulfillmentStatus) { this.fulfillmentStatus = fulfillmentStatus; }
}
