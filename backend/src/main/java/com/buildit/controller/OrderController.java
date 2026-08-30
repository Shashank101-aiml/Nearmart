package com.buildit.controller;

import com.buildit.dto.response.OrderResponse;
import com.buildit.security.CustomUserDetails;
import com.buildit.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(principal.getUser().getId()));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(orderService.listOrders(principal.getUser().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(principal.getUser().getId(), id));
    }
}
