package com.buildit.messaging.events;

public class InventoryUpdatedEvent {
    private Long productId;
    private Integer newQuantity;

    public InventoryUpdatedEvent() {}
    public InventoryUpdatedEvent(Long productId, Integer newQuantity) {
        this.productId = productId;
        this.newQuantity = newQuantity;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getNewQuantity() { return newQuantity; }
    public void setNewQuantity(Integer newQuantity) { this.newQuantity = newQuantity; }
}
