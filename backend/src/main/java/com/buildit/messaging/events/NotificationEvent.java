package com.buildit.messaging.events;

public class NotificationEvent {
    private Long customerId;
    private String message;

    public NotificationEvent() {}
    public NotificationEvent(Long customerId, String message) {
        this.customerId = customerId;
        this.message = message;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
