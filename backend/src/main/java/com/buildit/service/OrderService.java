package com.buildit.service;

import com.buildit.dto.request.VerifyPaymentRequest;
import com.buildit.dto.response.OrderResponse;
import com.buildit.dto.response.VendorOrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long customerId);
    List<OrderResponse> listOrders(Long customerId);
    OrderResponse getOrder(Long customerId, Long orderId);
    List<VendorOrderResponse> listVendorOrders(Long vendorId);
    VendorOrderResponse getVendorOrder(Long vendorId, Long orderId);
    OrderResponse verifyPayment(Long customerId, Long orderId, VerifyPaymentRequest request);
    OrderResponse retryPayment(Long customerId, Long orderId);
}
