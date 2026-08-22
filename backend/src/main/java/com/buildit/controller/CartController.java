package com.buildit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @GetMapping
    public ResponseEntity<String> getCart() {
        return ResponseEntity.ok("Current cart items");
    }
}
