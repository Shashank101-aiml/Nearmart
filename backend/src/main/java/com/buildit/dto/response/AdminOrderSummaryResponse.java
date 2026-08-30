package com.buildit.dto.response;

import java.time.LocalDateTime;

public class AdminOrderSummaryResponse {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private Long customerId;
    private String customerName;
    private String customerUsername;
    private Integer itemCount;
    private Double total;

    public AdminOrderSummaryResponse(Long id, String status, LocalDateTime createdAt, Long customerId,
                                      String customerName, String customerUsername, Integer itemCount,
                                      Double total) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerUsername = customerUsername;
        this.itemCount = itemCount;
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
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
