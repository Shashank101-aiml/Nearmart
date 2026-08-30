package com.buildit.service;

import com.buildit.dto.response.AdminOrderResponse;
import com.buildit.dto.response.AdminOrderSummaryResponse;
import com.buildit.dto.response.AdminUserResponse;
import com.buildit.dto.response.AdminVendorResponse;

import java.util.List;

public interface AdminService {
    List<AdminUserResponse> listUsers();
    List<AdminVendorResponse> listVendors();
    AdminUserResponse setUserEnabled(Long actingAdminId, Long targetUserId, boolean enabled);
    List<AdminOrderSummaryResponse> listAllOrders();
    AdminOrderResponse getOrder(Long orderId);
}
