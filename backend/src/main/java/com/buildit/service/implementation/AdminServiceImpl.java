package com.buildit.service.implementation;

import com.buildit.dto.response.AdminOrderItemResponse;
import com.buildit.dto.response.AdminOrderResponse;
import com.buildit.dto.response.AdminOrderSummaryResponse;
import com.buildit.dto.response.AdminUserResponse;
import com.buildit.dto.response.AdminVendorResponse;
import com.buildit.entity.Order;
import com.buildit.entity.OrderItem;
import com.buildit.entity.User;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.repository.OrderItemRepository;
import com.buildit.repository.OrderRepository;
import com.buildit.repository.UserRepository;
import com.buildit.repository.VendorRepository;
import com.buildit.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminServiceImpl(UserRepository userRepository, VendorRepository vendorRepository,
                             OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(u -> new AdminUserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name(),
                u.getEnabled()))
            .toList();
    }

    @Override
    public List<AdminVendorResponse> listVendors() {
        return vendorRepository.findAll().stream()
            .map(v -> new AdminVendorResponse(v.getId(), v.getUser().getUsername(), v.getUser().getEmail(),
                v.getUser().getEnabled(), v.getStoreName(), v.getLocation()))
            .toList();
    }

    @Override
    @Transactional
    public AdminUserResponse setUserEnabled(Long actingAdminId, Long targetUserId, boolean enabled) {
        if (!enabled && actingAdminId.equals(targetUserId)) {
            throw new BadRequestException("You cannot disable your own account");
        }

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(enabled);
        user = userRepository.save(user);

        return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(),
            user.getEnabled());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminOrderSummaryResponse> listAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toSummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toDetail(order, items);
    }

    private AdminOrderSummaryResponse toSummary(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        double total = items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();

        return new AdminOrderSummaryResponse(
            order.getId(),
            order.getStatus().name(),
            order.getCreatedAt(),
            order.getCustomer().getId(),
            order.getCustomer().getName(),
            order.getCustomer().getUser().getUsername(),
            items.size(),
            total
        );
    }

    private AdminOrderResponse toDetail(Order order, List<OrderItem> items) {
        List<AdminOrderItemResponse> itemResponses = items.stream()
            .map(item -> new AdminOrderItemResponse(
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice() * item.getQuantity(),
                item.getProduct() != null ? item.getProduct().getVendor().getId() : null,
                item.getProduct() != null ? item.getProduct().getVendor().getStoreName() : null,
                item.getFulfillmentStatus().name()
            ))
            .toList();

        double total = itemResponses.stream().mapToDouble(AdminOrderItemResponse::getLineTotal).sum();

        return new AdminOrderResponse(
            order.getId(),
            order.getStatus().name(),
            order.getCreatedAt(),
            order.getCustomer().getId(),
            order.getCustomer().getName(),
            order.getCustomer().getUser().getUsername(),
            itemResponses,
            total
        );
    }
}
