package com.buildit.controller;

import com.buildit.dto.request.AddCartItemRequest;
import com.buildit.dto.request.UpdateCartItemRequest;
import com.buildit.dto.response.CartResponse;
import com.buildit.security.CustomUserDetails;
import com.buildit.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getUser().getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(principal.getUser().getId(), request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long productId,
                                                     @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(principal.getUser().getId(), productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getUser().getId(), productId));
    }
}
