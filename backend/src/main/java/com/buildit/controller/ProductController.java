package com.buildit.controller;

import com.buildit.dto.request.ProductRequest;
import com.buildit.dto.response.ProductResponse;
import com.buildit.security.CustomUserDetails;
import com.buildit.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listAvailable() {
        return ResponseEntity.ok(productService.listAvailable());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ProductResponse>> listMine(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(productService.listOwnProducts(principal.getUser().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productService.create(principal.getUser().getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(principal.getUser().getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        productService.delete(principal.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }
}
