package com.buildit.service;

import com.buildit.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long customerId);
    List<OrderResponse> listOrders(Long customerId);
    OrderResponse getOrder(Long customerId, Long orderId);
}
