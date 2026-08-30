package com.buildit.controller;

import com.buildit.dto.request.UpdateProductStatusRequest;
import com.buildit.dto.request.UpdateUserStatusRequest;
import com.buildit.dto.response.AdminOrderResponse;
import com.buildit.dto.response.AdminOrderSummaryResponse;
import com.buildit.dto.response.AdminUserResponse;
import com.buildit.dto.response.AdminVendorResponse;
import com.buildit.dto.response.ProductResponse;
import com.buildit.security.CustomUserDetails;
import com.buildit.service.AdminService;
import com.buildit.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final ProductService productService;

    public AdminController(AdminService adminService, ProductService productService) {
        this.adminService = adminService;
        this.productService = productService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(
            adminService.setUserEnabled(principal.getUser().getId(), id, request.getEnabled()));
    }

    @GetMapping("/vendors")
    public ResponseEntity<List<AdminVendorResponse>> listVendors() {
        return ResponseEntity.ok(adminService.listVendors());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderSummaryResponse>> listOrders() {
        return ResponseEntity.ok(adminService.listAllOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<AdminOrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getOrder(id));
    }

    @PatchMapping("/products/{id}/status")
    public ResponseEntity<ProductResponse> updateProductStatus(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateProductStatusRequest request) {
        return ResponseEntity.ok(productService.setAvailability(id, request.getAvailable()));
    }
}
