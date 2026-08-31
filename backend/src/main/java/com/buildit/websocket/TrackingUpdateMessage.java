package com.buildit.websocket;

public class TrackingUpdateMessage {
    private Long orderId;
    private Long itemId;
    private String fulfillmentStatus;

    public TrackingUpdateMessage(Long orderId, Long itemId, String fulfillmentStatus) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getFulfillmentStatus() { return fulfillmentStatus; }
    public void setFulfillmentStatus(String fulfillmentStatus) { this.fulfillmentStatus = fulfillmentStatus; }
}
