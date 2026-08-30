package com.buildit.service;

import com.buildit.dto.request.AddCartItemRequest;
import com.buildit.dto.request.UpdateCartItemRequest;
import com.buildit.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long customerId);
    CartResponse addItem(Long customerId, AddCartItemRequest request);
    CartResponse updateItemQuantity(Long customerId, Long productId, UpdateCartItemRequest request);
    CartResponse removeItem(Long customerId, Long productId);
}
