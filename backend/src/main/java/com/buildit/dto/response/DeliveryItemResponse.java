package com.buildit.dto.response;

public class DeliveryItemResponse {
    private Long id;
    private Long orderId;
    private String productTitle;
    private Double unitPrice;
    private Integer quantity;
    private Double lineTotal;
    private String fulfillmentStatus;
    private String customerName;
    private String imageUrl;

    public DeliveryItemResponse(Long id, Long orderId, String productTitle, Double unitPrice, Integer quantity,
                                 Double lineTotal, String fulfillmentStatus, String customerName, String imageUrl) {
        this.id = id;
        this.orderId = orderId;
        this.productTitle = productTitle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.fulfillmentStatus = fulfillmentStatus;
        this.customerName = customerName;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getLineTotal() { return lineTotal; }
    public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
    public String getFulfillmentStatus() { return fulfillmentStatus; }
    public void setFulfillmentStatus(String fulfillmentStatus) { this.fulfillmentStatus = fulfillmentStatus; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
