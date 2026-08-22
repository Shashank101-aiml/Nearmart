package com.buildit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @GetMapping("/{id}")
    public ResponseEntity<String> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok("Customer details for " + id);
    }
}
