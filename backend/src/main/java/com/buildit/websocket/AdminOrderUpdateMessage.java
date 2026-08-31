package com.buildit.websocket;

public class AdminOrderUpdateMessage {
    private Long orderId;
    private String status;

    public AdminOrderUpdateMessage(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
