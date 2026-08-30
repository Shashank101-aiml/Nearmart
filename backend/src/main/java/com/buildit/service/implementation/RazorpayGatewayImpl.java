package com.buildit.service.implementation;

import com.buildit.exception.BadRequestException;
import com.buildit.service.RazorpayGateway;
import com.buildit.service.RazorpayOrderResult;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayGatewayImpl implements RazorpayGateway {
    private final RazorpayClient razorpayClient;
    private final String keySecret;

    public RazorpayGatewayImpl(RazorpayClient razorpayClient, @Value("${razorpay.key-secret}") String keySecret) {
        this.razorpayClient = razorpayClient;
        this.keySecret = keySecret;
    }

    @Override
    public RazorpayOrderResult createOrder(long amountInPaise, String receipt) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);
            com.razorpay.Order rzpOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = rzpOrder.get("id");
            return new RazorpayOrderResult(razorpayOrderId);
        } catch (RazorpayException e) {
            throw new BadRequestException("Payment provider error: could not create order");
        }
    }

    @Override
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", razorpayOrderId);
        options.put("razorpay_payment_id", razorpayPaymentId);
        options.put("razorpay_signature", razorpaySignature);
        try {
            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            return false;
        }
    }
}
