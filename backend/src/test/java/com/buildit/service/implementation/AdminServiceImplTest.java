package com.buildit.service.implementation;

import com.buildit.dto.response.AdminOrderResponse;
import com.buildit.dto.response.AdminOrderSummaryResponse;
import com.buildit.dto.response.AdminUserResponse;
import com.buildit.dto.response.AdminVendorResponse;
import com.buildit.entity.Customer;
import com.buildit.entity.Order;
import com.buildit.entity.OrderItem;
import com.buildit.entity.Product;
import com.buildit.entity.User;
import com.buildit.entity.Vendor;
import com.buildit.enums.OrderStatus;
import com.buildit.enums.UserRole;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.repository.OrderItemRepository;
import com.buildit.repository.OrderRepository;
import com.buildit.repository.UserRepository;
import com.buildit.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private VendorRepository vendorRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User userWithId(Long id, String username, UserRole role, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }

    private Vendor vendorFor(User user, String storeName) {
        Vendor vendor = new Vendor();
        vendor.setId(user.getId());
        vendor.setUser(user);
        vendor.setStoreName(storeName);
        vendor.setLocation("Downtown");
        return vendor;
    }

    private Customer customerWithIdAndName(Long id, String name, User user) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setUser(user);
        return customer;
    }

    private Order orderWithId(Long id, Customer customer) {
        Order order = new Order();
        order.setId(id);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private Product productOwnedBy(Long productId, Vendor vendor, double price) {
        Product product = new Product();
        product.setId(productId);
        product.setTitle("Product " + productId);
        product.setPrice(price);
        product.setVendor(vendor);
        return product;
    }

    private OrderItem orderItemFor(Order order, Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductTitle(product != null ? product.getTitle() : "Deleted product");
        item.setUnitPrice(product != null ? product.getPrice() : 5.0);
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void listUsersReturnsAllUsersWithRoleAndEnabledStatus() {
        User customer = userWithId(1L, "jdoe", UserRole.CUSTOMER, true);
        User disabledVendor = userWithId(2L, "vsmith", UserRole.VENDOR, false);
        when(userRepository.findAll()).thenReturn(List.of(customer, disabledVendor));

        List<AdminUserResponse> results = adminService.listUsers();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getRole()).isEqualTo("CUSTOMER");
        assertThat(results.get(0).getEnabled()).isTrue();
        assertThat(results.get(1).getRole()).isEqualTo("VENDOR");
        assertThat(results.get(1).getEnabled()).isFalse();
    }

    @Test
    void listVendorsReturnsStoreDetailsAndEnabledStatus() {
        User user = userWithId(3L, "vsmith", UserRole.VENDOR, false);
        Vendor vendor = vendorFor(user, "Acme Store");
        when(vendorRepository.findAll()).thenReturn(List.of(vendor));

        List<AdminVendorResponse> results = adminService.listVendors();

        assertThat(results).hasSize(1);
        AdminVendorResponse response = results.get(0);
        assertThat(response.getStoreName()).isEqualTo("Acme Store");
        assertThat(response.getEnabled()).isFalse();
    }

    @Test
    void listAllOrdersAggregatesTotalAndItemCountAcrossVendors() {
        User customerUser = userWithId(1L, "jdoe", UserRole.CUSTOMER, true);
        Customer customer = customerWithIdAndName(1L, "Jane Doe", customerUser);
        Order order = orderWithId(100L, customer);

        Vendor vendorA = vendorFor(userWithId(10L, "vendorA", UserRole.VENDOR, true), "Vendor A");
        Vendor vendorB = vendorFor(userWithId(20L, "vendorB", UserRole.VENDOR, true), "Vendor B");
        OrderItem itemA = orderItemFor(order, productOwnedBy(5L, vendorA, 3.0), 2);
        OrderItem itemB = orderItemFor(order, productOwnedBy(6L, vendorB, 7.0), 1);

        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(itemA, itemB));

        List<AdminOrderSummaryResponse> results = adminService.listAllOrders();

        assertThat(results).hasSize(1);
        AdminOrderSummaryResponse response = results.get(0);
        assertThat(response.getItemCount()).isEqualTo(2);
        assertThat(response.getTotal()).isEqualTo(13.0);
        assertThat(response.getCustomerName()).isEqualTo("Jane Doe");
    }

    @Test
    void getOrderReturnsFullDetailWithPerItemVendorIdentity() {
        User customerUser = userWithId(1L, "jdoe", UserRole.CUSTOMER, true);
        Customer customer = customerWithIdAndName(1L, "Jane Doe", customerUser);
        Order order = orderWithId(100L, customer);

        Vendor vendorA = vendorFor(userWithId(10L, "vendorA", UserRole.VENDOR, true), "Vendor A");
        Vendor vendorB = vendorFor(userWithId(20L, "vendorB", UserRole.VENDOR, true), "Vendor B");
        OrderItem itemA = orderItemFor(order, productOwnedBy(5L, vendorA, 3.0), 2);
        OrderItem itemB = orderItemFor(order, productOwnedBy(6L, vendorB, 7.0), 1);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(itemA, itemB));

        AdminOrderResponse response = adminService.getOrder(100L);

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getVendorId()).isEqualTo(10L);
        assertThat(response.getItems().get(0).getVendorStoreName()).isEqualTo("Vendor A");
        assertThat(response.getItems().get(1).getVendorId()).isEqualTo(20L);
        assertThat(response.getItems().get(1).getVendorStoreName()).isEqualTo("Vendor B");
        assertThat(response.getTotal()).isEqualTo(13.0);
    }

    @Test
    void getOrderHandlesItemWithDeletedProduct() {
        User customerUser = userWithId(1L, "jdoe", UserRole.CUSTOMER, true);
        Customer customer = customerWithIdAndName(1L, "Jane Doe", customerUser);
        Order order = orderWithId(100L, customer);

        OrderItem orphanedItem = orderItemFor(order, null, 3);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(orphanedItem));

        AdminOrderResponse response = adminService.getOrder(100L);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getVendorId()).isNull();
        assertThat(response.getItems().get(0).getVendorStoreName()).isNull();
        assertThat(response.getItems().get(0).getProductTitle()).isEqualTo("Deleted product");
        assertThat(response.getTotal()).isEqualTo(15.0);
    }

    @Test
    void setUserEnabledDisablesTargetUser() {
        User target = userWithId(5L, "jdoe", UserRole.CUSTOMER, true);
        when(userRepository.findById(5L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserResponse response = adminService.setUserEnabled(1L, 5L, false);

        assertThat(response.getEnabled()).isFalse();
        verify(userRepository).save(argThat(u -> !u.getEnabled()));
    }

    @Test
    void setUserEnabledReenablesTargetUser() {
        User target = userWithId(5L, "jdoe", UserRole.CUSTOMER, false);
        when(userRepository.findById(5L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserResponse response = adminService.setUserEnabled(1L, 5L, true);

        assertThat(response.getEnabled()).isTrue();
    }

    @Test
    void setUserEnabledRejectsSelfDisable() {
        assertThatThrownBy(() -> adminService.setUserEnabled(1L, 1L, false))
            .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void setUserEnabledAllowsSelfEnable() {
        User self = userWithId(1L, "admin", UserRole.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(self));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserResponse response = adminService.setUserEnabled(1L, 1L, true);

        assertThat(response.getEnabled()).isTrue();
    }

    @Test
    void setUserEnabledThrowsWhenUserNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.setUserEnabled(1L, 404L, false))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
