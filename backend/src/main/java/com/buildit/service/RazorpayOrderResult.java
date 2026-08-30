package com.buildit.service;

public class RazorpayOrderResult {
    private final String razorpayOrderId;

    public RazorpayOrderResult(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
}
