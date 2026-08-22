package com.buildit.messaging.events;

public class OrderCreatedEvent {
    private Long orderId;
    private Long customerId;

    public OrderCreatedEvent() {}
    public OrderCreatedEvent(Long orderId, Long customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
