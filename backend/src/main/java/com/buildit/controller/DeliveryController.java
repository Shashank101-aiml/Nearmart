package com.buildit.controller;

import com.buildit.dto.request.UpdateAvailabilityRequest;
import com.buildit.dto.request.UpdateItemFulfillmentRequest;
import com.buildit.dto.response.DeliveryItemResponse;
import com.buildit.dto.response.DeliveryPartnerResponse;
import com.buildit.security.CustomUserDetails;
import com.buildit.service.DeliveryPartnerService;
import com.buildit.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    private final OrderService orderService;
    private final DeliveryPartnerService deliveryPartnerService;

    public DeliveryController(OrderService orderService, DeliveryPartnerService deliveryPartnerService) {
        this.orderService = orderService;
        this.deliveryPartnerService = deliveryPartnerService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<DeliveryItemResponse>> listAvailable() {
        return ResponseEntity.ok(orderService.listAvailableDeliveryItems());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DeliveryItemResponse>> listMine(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(orderService.listMyDeliveryItems(principal.getUser().getId()));
    }

    @PostMapping("/items/{itemId}/claim")
    public ResponseEntity<DeliveryItemResponse> claim(@AuthenticationPrincipal CustomUserDetails principal,
                                                        @PathVariable Long itemId) {
        return ResponseEntity.ok(orderService.claimDeliveryItem(principal.getUser().getId(), itemId));
    }

    @PatchMapping("/items/{itemId}/status")
    public ResponseEntity<DeliveryItemResponse> updateStatus(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @PathVariable Long itemId,
                                                                @Valid @RequestBody UpdateItemFulfillmentRequest request) {
        return ResponseEntity.ok(
            orderService.updateDeliveryItemStatus(principal.getUser().getId(), itemId, request.getStatus()));
    }

    @GetMapping("/profile")
    public ResponseEntity<DeliveryPartnerResponse> getProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(deliveryPartnerService.getProfile(principal.getUser().getId()));
    }

    @PatchMapping("/availability")
    public ResponseEntity<DeliveryPartnerResponse> setAvailability(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(
            deliveryPartnerService.setAvailability(principal.getUser().getId(), request.getAvailable()));
    }
}
