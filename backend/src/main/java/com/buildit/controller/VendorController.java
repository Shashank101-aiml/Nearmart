package com.buildit.controller;

import com.buildit.dto.response.ProductResponse;
import com.buildit.dto.response.VendorResponse;
import com.buildit.service.ProductService;
import com.buildit.service.VendorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    private final VendorService vendorService;
    private final ProductService productService;

    public VendorController(VendorService vendorService, ProductService productService) {
        this.vendorService = vendorService;
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getVendor(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getById(id));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductResponse>> getVendorProducts(@PathVariable Long id) {
        return ResponseEntity.ok(productService.listByVendor(id));
    }
}
