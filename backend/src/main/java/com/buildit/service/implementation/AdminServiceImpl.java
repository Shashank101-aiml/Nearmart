package com.buildit.service.implementation;

import com.buildit.dto.request.UpdateVendorRequest;
import com.buildit.dto.response.AdminOrderItemResponse;
import com.buildit.dto.response.AdminOrderResponse;
import com.buildit.dto.response.AdminOrderSummaryResponse;
import com.buildit.dto.response.AdminUserResponse;
import com.buildit.dto.response.AdminVendorResponse;
import com.buildit.entity.Customer;
import com.buildit.entity.Order;
import com.buildit.entity.OrderItem;
import com.buildit.entity.User;
import com.buildit.entity.Vendor;
import com.buildit.enums.UserRole;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.DuplicateResourceException;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.repository.CartRepository;
import com.buildit.repository.CustomerRepository;
import com.buildit.repository.NotificationRepository;
import com.buildit.repository.OrderItemRepository;
import com.buildit.repository.OrderRepository;
import com.buildit.repository.ProductRepository;
import com.buildit.repository.UserRepository;
import com.buildit.repository.VendorRepository;
import com.buildit.service.AdminService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminServiceImpl(UserRepository userRepository, VendorRepository vendorRepository,
                             CustomerRepository customerRepository, CartRepository cartRepository,
                             NotificationRepository notificationRepository, ProductRepository productRepository,
                             OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(u -> new AdminUserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getPhoneNumber(),
                u.getRole().name(), u.getEnabled()))
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

        return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getPhoneNumber(),
            user.getRole().name(), user.getEnabled());
    }

    @Override
    @Transactional
    public void deleteUser(Long actingAdminId, Long targetUserId) {
        if (actingAdminId.equals(targetUserId)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be deleted");
        }

        if (user.getRole() == UserRole.CUSTOMER) {
            if (orderRepository.existsByCustomerId(targetUserId)) {
                throw new BadRequestException("Cannot delete a customer with order history");
            }
            cartRepository.findByCustomerId(targetUserId).ifPresent(cartRepository::delete);
            notificationRepository.deleteByCustomerId(targetUserId);
            Customer customer = customerRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            customerRepository.delete(customer);
        } else if (user.getRole() == UserRole.VENDOR) {
            if (productRepository.existsByVendorId(targetUserId)) {
                throw new BadRequestException("Cannot delete a vendor with product listings");
            }
            Vendor vendor = vendorRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
            vendorRepository.delete(vendor);
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public AdminVendorResponse updateVendor(Long vendorId, UpdateVendorRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setStoreName(request.getStoreName());
        vendor.setLocation(request.getLocation());
        vendor = vendorRepository.save(vendor);

        String newUsername = request.getUsername();
        if (newUsername != null && !newUsername.equals(vendor.getUser().getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new DuplicateResourceException("Username already taken");
            }
            User user = vendor.getUser();
            user.setUsername(newUsername);
            userRepository.save(user);
        }

        return new AdminVendorResponse(vendor.getId(), vendor.getUser().getUsername(), vendor.getUser().getEmail(),
            vendor.getUser().getEnabled(), vendor.getStoreName(), vendor.getLocation());
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
                item.getFulfillmentStatus().name(),
                item.getProduct() != null ? item.getProduct().getImageUrl() : null
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
