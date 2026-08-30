package com.buildit.controller;

import com.buildit.dto.request.UpdateItemFulfillmentRequest;
import com.buildit.dto.response.VendorOrderResponse;
import com.buildit.security.CustomUserDetails;
import com.buildit.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/orders")
public class VendorOrderController {
    private final OrderService orderService;

    public VendorOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<VendorOrderResponse>> listOrders(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(orderService.listVendorOrders(principal.getUser().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorOrderResponse> getOrder(@AuthenticationPrincipal CustomUserDetails principal,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getVendorOrder(principal.getUser().getId(), id));
    }

    @PatchMapping("/{orderId}/items/{itemId}/status")
    public ResponseEntity<VendorOrderResponse> updateItemFulfillmentStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemFulfillmentRequest request) {
        return ResponseEntity.ok(orderService.updateItemFulfillmentStatus(
            principal.getUser().getId(), orderId, itemId, request.getStatus()));
    }
}
