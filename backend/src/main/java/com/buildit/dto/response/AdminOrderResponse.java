package com.buildit.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class AdminOrderResponse {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private Long customerId;
    private String customerName;
    private String customerUsername;
    private List<AdminOrderItemResponse> items;
    private Double total;

    public AdminOrderResponse(Long id, String status, LocalDateTime createdAt, Long customerId,
                               String customerName, String customerUsername,
                               List<AdminOrderItemResponse> items, Double total) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerUsername = customerUsername;
        this.items = items;
        this.total = total;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
    public List<AdminOrderItemResponse> getItems() { return items; }
    public void setItems(List<AdminOrderItemResponse> items) { this.items = items; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
