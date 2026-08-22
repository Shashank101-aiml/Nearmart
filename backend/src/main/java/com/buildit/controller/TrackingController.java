package com.buildit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    @GetMapping("/{orderId}")
    public ResponseEntity<String> getTracking(@PathVariable Long orderId) {
        return ResponseEntity.ok("Tracking info for order " + orderId);
    }
}
