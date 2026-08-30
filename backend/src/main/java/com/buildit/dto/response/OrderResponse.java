package com.buildit.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private Double total;

    public OrderResponse(Long id, String status, LocalDateTime createdAt, List<OrderItemResponse> items,
                          Double total) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
        this.total = total;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
