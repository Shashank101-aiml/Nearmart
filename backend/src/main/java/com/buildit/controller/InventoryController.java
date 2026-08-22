package com.buildit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @GetMapping("/{productId}")
    public ResponseEntity<String> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok("Stock level for product " + productId);
    }

    @PostMapping
    public ResponseEntity<String> createStock(@RequestBody String payload) {
        return ResponseEntity.ok("Created stock: " + payload);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<String> updateStock(@PathVariable Long productId, @RequestBody String payload) {
        return ResponseEntity.ok("Updated stock for product " + productId + ": " + payload);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteStock(@PathVariable Long productId) {
        return ResponseEntity.ok("Deleted stock for product " + productId);
    }
}
