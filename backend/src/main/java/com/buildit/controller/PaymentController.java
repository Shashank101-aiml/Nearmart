package com.buildit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @PostMapping("/process")
    public ResponseEntity<String> processPayment() {
        return ResponseEntity.ok("Payment processed");
    }
}
