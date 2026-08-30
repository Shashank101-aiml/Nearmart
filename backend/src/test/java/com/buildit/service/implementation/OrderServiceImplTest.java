package com.buildit.service.implementation;

import com.buildit.dto.response.OrderResponse;
import com.buildit.dto.response.VendorOrderResponse;
import com.buildit.entity.Cart;
import com.buildit.entity.CartItem;
import com.buildit.entity.Customer;
import com.buildit.entity.Inventory;
import com.buildit.entity.Order;
import com.buildit.entity.OrderItem;
import com.buildit.entity.Product;
import com.buildit.entity.Vendor;
import com.buildit.enums.OrderStatus;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.exception.UnauthorizedException;
import com.buildit.messaging.events.OrderCreatedEvent;
import com.buildit.messaging.producer.OrderProducer;
import com.buildit.repository.CartItemRepository;
import com.buildit.repository.CartRepository;
import com.buildit.repository.InventoryRepository;
import com.buildit.repository.OrderItemRepository;
import com.buildit.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @BeforeEach
    void initTransactionSynchronization() {
        // placeOrder() registers a transaction synchronization to publish its event only
        // after commit. There's no real Spring transaction here, so simulate one: activate
        // synchronization before each test and fire afterCommit() manually where needed.
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private OrderProducer orderProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customerWithId(Long id) {
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    private Cart cartFor(Long cartId, Customer customer) {
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setCustomer(customer);
        return cart;
    }

    private Product productWithId(Long id, double price) {
        Product product = new Product();
        product.setId(id);
        product.setTitle("Widget");
        product.setPrice(price);
        return product;
    }

    private CartItem cartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(50L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private Inventory inventoryOf(Long productId, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    private Order orderWithId(Long id, Customer customer) {
        Order order = new Order();
        order.setId(id);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(java.time.LocalDateTime.now());
        return order;
    }

    private Customer customerWithIdAndName(Long id, String name) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        return customer;
    }

    private Vendor vendorWithId(Long id) {
        Vendor vendor = new Vendor();
        vendor.setId(id);
        vendor.setStoreName("Vendor " + id);
        return vendor;
    }

    private Product productOwnedBy(Long productId, Long vendorId, double price) {
        Product product = new Product();
        product.setId(productId);
        product.setTitle("Product " + productId);
        product.setPrice(price);
        product.setVendor(vendorWithId(vendorId));
        return product;
    }

    private OrderItem orderItemFor(Order order, Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductTitle(product.getTitle());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void placeOrderCreatesOrderAndClearsCart() {
        Customer customer = customerWithId(1L);
        Cart cart = cartFor(10L, customer);
        Product product = productWithId(5L, 3.5);
        CartItem item = cartItem(cart, product, 2);

        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(item));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 10)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            o.setCreatedAt(java.time.LocalDateTime.now());
            return o;
        });
        OrderItem savedItem = new OrderItem();
        savedItem.setProduct(product);
        savedItem.setProductTitle("Widget");
        savedItem.setUnitPrice(3.5);
        savedItem.setQuantity(2);
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(savedItem));

        OrderResponse response = orderService.placeOrder(1L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("PLACED");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(7.0);

        // No real transaction is committing in this test, so the event-publishing
        // synchronization placeOrder() registered must be fired manually here.
        verify(orderProducer, never()).sendOrderCreatedEvent(any());
        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
        }

        verify(orderItemRepository).save(argThat(oi ->
            oi.getProductTitle().equals("Widget") && oi.getUnitPrice() == 3.5 && oi.getQuantity() == 2));
        verify(cartItemRepository).deleteAll(List.of(item));
        verify(orderProducer).sendOrderCreatedEvent(argThat((OrderCreatedEvent e) ->
            e.getOrderId().equals(100L) && e.getCustomerId().equals(1L)));
    }

    @Test
    void placeOrderThrowsBadRequestWhenCartMissing() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(1L))
            .isInstanceOf(BadRequestException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrderThrowsBadRequestWhenCartEmpty() {
        Cart cart = cartFor(10L, customerWithId(1L));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder(1L))
            .isInstanceOf(BadRequestException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrderThrowsBadRequestWhenStockInsufficient() {
        Customer customer = customerWithId(1L);
        Cart cart = cartFor(10L, customer);
        Product product = productWithId(5L, 3.5);
        CartItem item = cartItem(cart, product, 5);

        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(item));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 2)));

        assertThatThrownBy(() -> orderService.placeOrder(1L))
            .isInstanceOf(BadRequestException.class);

        verify(orderRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteAll(any());
    }

    @Test
    void listOrdersReturnsNewestFirst() {
        Customer customer = customerWithId(1L);
        Order order = orderWithId(100L, customer);
        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of());

        List<OrderResponse> results = orderService.listOrders(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(100L);
        verify(orderRepository).findByCustomerIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getOrderSucceedsForOwner() {
        Customer customer = customerWithId(1L);
        Order order = orderWithId(100L, customer);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of());

        OrderResponse response = orderService.getOrder(1L, 100L);

        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    void getOrderThrowsUnauthorizedWhenNotOwner() {
        Customer customer = customerWithId(1L);
        Order order = orderWithId(100L, customer);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrder(2L, 100L))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getOrderThrowsResourceNotFoundWhenMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(1L, 404L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listVendorOrdersShowsOnlyOwnItemsWhenOrderHasMultipleVendors() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product vendorAProduct = productOwnedBy(5L, 10L, 3.0);
        Product vendorBProduct = productOwnedBy(6L, 20L, 7.0);
        OrderItem itemA = orderItemFor(order, vendorAProduct, 2);
        OrderItem itemB = orderItemFor(order, vendorBProduct, 1);

        when(orderItemRepository.findByProductVendorId(10L)).thenReturn(List.of(itemA));

        List<VendorOrderResponse> results = orderService.listVendorOrders(10L);

        assertThat(results).hasSize(1);
        VendorOrderResponse response = results.get(0);
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCustomerName()).isEqualTo("Jane Doe");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductId()).isEqualTo(5L);
        assertThat(response.getVendorSubtotal()).isEqualTo(6.0);
    }

    @Test
    void listVendorOrdersGroupsMultipleOrdersCorrectly() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order orderOne = orderWithId(100L, customer);
        Order orderTwo = orderWithId(101L, customer);
        Product product = productOwnedBy(5L, 10L, 3.0);

        OrderItem itemInOrderOne = orderItemFor(orderOne, product, 1);
        OrderItem itemInOrderTwo = orderItemFor(orderTwo, product, 4);

        when(orderItemRepository.findByProductVendorId(10L)).thenReturn(List.of(itemInOrderOne, itemInOrderTwo));

        List<VendorOrderResponse> results = orderService.listVendorOrders(10L);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(VendorOrderResponse::getId).containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    void getVendorOrderSucceedsWhenVendorOwnsAtLeastOneItem() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product vendorAProduct = productOwnedBy(5L, 10L, 3.0);
        Product vendorBProduct = productOwnedBy(6L, 20L, 7.0);
        OrderItem itemA = orderItemFor(order, vendorAProduct, 2);
        OrderItem itemB = orderItemFor(order, vendorBProduct, 1);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(itemA, itemB));

        VendorOrderResponse response = orderService.getVendorOrder(10L, 100L);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductId()).isEqualTo(5L);
        assertThat(response.getVendorSubtotal()).isEqualTo(6.0);
    }

    @Test
    void getVendorOrderThrowsUnauthorizedWhenVendorHasNoOwnedItems() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product vendorBProduct = productOwnedBy(6L, 20L, 7.0);
        OrderItem itemB = orderItemFor(order, vendorBProduct, 1);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(itemB));

        assertThatThrownBy(() -> orderService.getVendorOrder(10L, 100L))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getVendorOrderThrowsResourceNotFoundWhenMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getVendorOrder(10L, 404L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
