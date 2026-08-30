package com.buildit.service;

public interface RazorpayGateway {
    RazorpayOrderResult createOrder(long amountInPaise, String receipt);
    boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
}
