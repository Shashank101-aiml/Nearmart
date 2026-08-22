package com.buildit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    @GetMapping("/{id}")
    public ResponseEntity<String> getVendor(@PathVariable Long id) {
        return ResponseEntity.ok("Vendor details for " + id);
    }
}
