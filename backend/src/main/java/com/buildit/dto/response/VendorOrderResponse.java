package com.buildit.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class VendorOrderResponse {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private String customerName;
    private List<OrderItemResponse> items;
    private Double vendorSubtotal;

    public VendorOrderResponse(Long id, String status, LocalDateTime createdAt, String customerName,
                                List<OrderItemResponse> items, Double vendorSubtotal) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.customerName = customerName;
        this.items = items;
        this.vendorSubtotal = vendorSubtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    public Double getVendorSubtotal() { return vendorSubtotal; }
    public void setVendorSubtotal(Double vendorSubtotal) { this.vendorSubtotal = vendorSubtotal; }
}
