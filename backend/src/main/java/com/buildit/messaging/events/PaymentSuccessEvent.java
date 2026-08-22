package com.buildit.messaging.events;

public class PaymentSuccessEvent {
    private Long paymentId;
    private Long orderId;

    public PaymentSuccessEvent() {}
    public PaymentSuccessEvent(Long paymentId, Long orderId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
