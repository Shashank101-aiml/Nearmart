package com.buildit.service.implementation;

import com.buildit.dto.request.VerifyPaymentRequest;
import com.buildit.dto.response.OrderResponse;
import com.buildit.dto.response.VendorOrderResponse;
import com.buildit.entity.Cart;
import com.buildit.entity.CartItem;
import com.buildit.entity.Customer;
import com.buildit.entity.Inventory;
import com.buildit.entity.Order;
import com.buildit.entity.OrderItem;
import com.buildit.entity.Payment;
import com.buildit.entity.Product;
import com.buildit.entity.Vendor;
import com.buildit.enums.ItemFulfillmentStatus;
import com.buildit.enums.OrderStatus;
import com.buildit.enums.PaymentStatus;
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
import com.buildit.repository.PaymentRepository;
import com.buildit.service.RazorpayGateway;
import com.buildit.service.RazorpayOrderResult;
import com.buildit.messaging.producer.NotificationProducer;
import com.buildit.websocket.AdminOrderPublisher;
import com.buildit.websocket.WebSocketPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @BeforeEach
    void initTransactionSynchronization() {
        // placeOrder()/verifyPayment() register a transaction synchronization to publish an event
        // only after commit. There's no real Spring transaction here, so simulate one: activate
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
    @Mock private RazorpayGateway razorpayGateway;
    @Mock private PaymentRepository paymentRepository;
    @Mock private WebSocketPublisher webSocketPublisher;
    @Mock private AdminOrderPublisher adminOrderPublisher;
    @Mock private NotificationProducer notificationProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void injectRazorpayKeyId() {
        ReflectionTestUtils.setField(orderService, "razorpayKeyId", "rzp_test_dummy");
    }

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
        return orderWithIdAndStatus(id, customer, OrderStatus.PLACED);
    }

    private Order orderWithIdAndStatus(Long id, Customer customer, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setCustomer(customer);
        order.setStatus(status);
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

    private Payment paymentFor(Order order, String razorpayOrderId, PaymentStatus status, double amount) {
        Payment payment = new Payment();
        payment.setId(900L);
        payment.setOrder(order);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setAmount(amount);
        payment.setStatus(status);
        return payment;
    }

    @Test
    void placeOrderCreatesPendingPaymentOrderAndRazorpayOrder() {
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
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(razorpayGateway.createOrder(anyLong(), anyString()))
            .thenReturn(new RazorpayOrderResult("order_rzp_test123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.placeOrder(1L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(7.0);
        assertThat(response.getRazorpayOrderId()).isEqualTo("order_rzp_test123");
        assertThat(response.getRazorpayKeyId()).isEqualTo("rzp_test_dummy");
        assertThat(response.getAmountInPaise()).isEqualTo(700L);

        verify(razorpayGateway).createOrder(700L, "order_100");
        verify(paymentRepository).save(argThat(p ->
            p.getRazorpayOrderId().equals("order_rzp_test123") && p.getStatus() == PaymentStatus.CREATED));
        verify(cartItemRepository).deleteAll(List.of(item));
    }

    @Test
    void placeOrderNoLongerPublishesOrderCreatedEventBeforePayment() {
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
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(razorpayGateway.createOrder(anyLong(), anyString()))
            .thenReturn(new RazorpayOrderResult("order_rzp_test123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.placeOrder(1L);

        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        verify(orderProducer, never()).sendOrderCreatedEvent(any());
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
    void placeOrderPropagatesBadRequestWhenGatewayFails() {
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
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(razorpayGateway.createOrder(anyLong(), anyString()))
            .thenThrow(new BadRequestException("Payment provider error: could not create order"));

        assertThatThrownBy(() -> orderService.placeOrder(1L))
            .isInstanceOf(BadRequestException.class);

        verify(paymentRepository, never()).save(any());
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
    void verifyPaymentMarksOrderPlacedAndPublishesEventOnValidSignature() {
        Customer customer = customerWithId(1L);
        Order order = orderWithIdAndStatus(100L, customer, OrderStatus.PENDING_PAYMENT);
        Payment payment = paymentFor(order, "order_rzp_test123", PaymentStatus.CREATED, 7.0);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));
        when(razorpayGateway.verifySignature("order_rzp_test123", "pay_abc", "sig_valid")).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of());

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_rzp_test123");
        request.setRazorpayPaymentId("pay_abc");
        request.setRazorpaySignature("sig_valid");

        OrderResponse response = orderService.verifyPayment(1L, 100L, request);

        assertThat(response.getStatus()).isEqualTo("PLACED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getRazorpayPaymentId()).isEqualTo("pay_abc");

        verify(orderProducer, never()).sendOrderCreatedEvent(any());
        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
        }
        verify(orderProducer).sendOrderCreatedEvent(argThat((OrderCreatedEvent e) ->
            e.getOrderId().equals(100L) && e.getCustomerId().equals(1L)));
        verify(adminOrderPublisher).broadcastOrderStatusChange(100L, "PLACED");
        verify(notificationProducer).sendNotification(eq(1L), anyString());
    }

    @Test
    void verifyPaymentMarksPaymentFailedOnInvalidSignature() {
        Customer customer = customerWithId(1L);
        Order order = orderWithIdAndStatus(100L, customer, OrderStatus.PENDING_PAYMENT);
        Payment payment = paymentFor(order, "order_rzp_test123", PaymentStatus.CREATED, 7.0);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));
        when(razorpayGateway.verifySignature(anyString(), anyString(), anyString())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_rzp_test123");
        request.setRazorpayPaymentId("pay_abc");
        request.setRazorpaySignature("sig_bad");

        assertThatThrownBy(() -> orderService.verifyPayment(1L, 100L, request))
            .isInstanceOf(BadRequestException.class);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(orderProducer, never()).sendOrderCreatedEvent(any());
        verify(adminOrderPublisher).broadcastOrderStatusChange(100L, "PAYMENT_FAILED");
        verify(notificationProducer).sendNotification(eq(1L), anyString());
    }

    @Test
    void verifyPaymentThrowsUnauthorizedWhenNotOwner() {
        Customer customer = customerWithId(1L);
        Order order = orderWithIdAndStatus(100L, customer, OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_rzp_test123");
        request.setRazorpayPaymentId("pay_abc");
        request.setRazorpaySignature("sig_valid");

        assertThatThrownBy(() -> orderService.verifyPayment(2L, 100L, request))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void verifyPaymentThrowsResourceNotFoundWhenOrderMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_rzp_test123");
        request.setRazorpayPaymentId("pay_abc");
        request.setRazorpaySignature("sig_valid");

        assertThatThrownBy(() -> orderService.verifyPayment(1L, 404L, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void retryPaymentResetsFailedOrderToPendingWithFreshRazorpayOrder() {
        Customer customer = customerWithId(1L);
        Order order = orderWithIdAndStatus(100L, customer, OrderStatus.PAYMENT_FAILED);
        Payment payment = paymentFor(order, "order_rzp_old", PaymentStatus.FAILED, 7.0);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));
        when(razorpayGateway.createOrder(anyLong(), anyString()))
            .thenReturn(new RazorpayOrderResult("order_rzp_new"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of());

        OrderResponse response = orderService.retryPayment(1L, 100L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(payment.getRazorpayPaymentId()).isNull();
        assertThat(response.getRazorpayOrderId()).isEqualTo("order_rzp_new");
    }

    @Test
    void retryPaymentRejectsAlreadyPlacedOrder() {
        Customer customer = customerWithId(1L);
        Order order = orderWithIdAndStatus(100L, customer, OrderStatus.PLACED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.retryPayment(1L, 100L))
            .isInstanceOf(BadRequestException.class);

        verify(razorpayGateway, never()).createOrder(anyLong(), anyString());
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
    void listVendorOrdersExcludesNonPlacedOrders() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order placedOrder = orderWithId(100L, customer);
        Order pendingOrder = orderWithIdAndStatus(101L, customer, OrderStatus.PENDING_PAYMENT);
        Product product = productOwnedBy(5L, 10L, 3.0);

        OrderItem itemInPlacedOrder = orderItemFor(placedOrder, product, 1);
        OrderItem itemInPendingOrder = orderItemFor(pendingOrder, product, 1);

        when(orderItemRepository.findByProductVendorId(10L))
            .thenReturn(List.of(itemInPlacedOrder, itemInPendingOrder));

        List<VendorOrderResponse> results = orderService.listVendorOrders(10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(100L);
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
    void getVendorOrderThrowsUnauthorizedWhenOrderNotYetPaid() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithIdAndStatus(100L, customer, OrderStatus.PENDING_PAYMENT);
        Product vendorAProduct = productOwnedBy(5L, 10L, 3.0);
        OrderItem itemA = orderItemFor(order, vendorAProduct, 2);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(itemA));

        assertThatThrownBy(() -> orderService.getVendorOrder(10L, 100L))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getVendorOrderThrowsResourceNotFoundWhenMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getVendorOrder(10L, 404L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItemFulfillmentStatusAdvancesProcessingToShipped() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product product = productOwnedBy(5L, 10L, 3.0);
        OrderItem item = orderItemFor(order, product, 2);
        item.setId(200L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(200L)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));

        VendorOrderResponse response =
            orderService.updateItemFulfillmentStatus(10L, 100L, 200L, ItemFulfillmentStatus.SHIPPED);

        assertThat(item.getFulfillmentStatus()).isEqualTo(ItemFulfillmentStatus.SHIPPED);
        assertThat(response.getItems().get(0).getFulfillmentStatus()).isEqualTo("SHIPPED");
        verify(orderItemRepository).save(item);
        verify(webSocketPublisher).notifyCustomer(eq(1L), any());
        verify(notificationProducer).sendNotification(eq(1L), anyString());
    }

    @Test
    void updateItemFulfillmentStatusAdvancesShippedToDelivered() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product product = productOwnedBy(5L, 10L, 3.0);
        OrderItem item = orderItemFor(order, product, 2);
        item.setId(200L);
        item.setFulfillmentStatus(ItemFulfillmentStatus.SHIPPED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(200L)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));

        VendorOrderResponse response =
            orderService.updateItemFulfillmentStatus(10L, 100L, 200L, ItemFulfillmentStatus.DELIVERED);

        assertThat(item.getFulfillmentStatus()).isEqualTo(ItemFulfillmentStatus.DELIVERED);
        assertThat(response.getItems().get(0).getFulfillmentStatus()).isEqualTo("DELIVERED");
        verify(webSocketPublisher).notifyCustomer(eq(1L), any());
        verify(notificationProducer).sendNotification(eq(1L), anyString());
    }

    @Test
    void updateItemFulfillmentStatusThrowsBadRequestWhenStatusUnchanged() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product product = productOwnedBy(5L, 10L, 3.0);
        OrderItem item = orderItemFor(order, product, 2);
        item.setId(200L);
        item.setFulfillmentStatus(ItemFulfillmentStatus.SHIPPED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.updateItemFulfillmentStatus(10L, 100L, 200L, ItemFulfillmentStatus.SHIPPED))
            .isInstanceOf(BadRequestException.class);

        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void updateItemFulfillmentStatusThrowsBadRequestWhenMovingBackward() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product product = productOwnedBy(5L, 10L, 3.0);
        OrderItem item = orderItemFor(order, product, 2);
        item.setId(200L);
        item.setFulfillmentStatus(ItemFulfillmentStatus.SHIPPED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.updateItemFulfillmentStatus(10L, 100L, 200L, ItemFulfillmentStatus.PROCESSING))
            .isInstanceOf(BadRequestException.class);

        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void updateItemFulfillmentStatusThrowsUnauthorizedWhenItemNotOwnedByVendor() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Product product = productOwnedBy(5L, 20L, 3.0);
        OrderItem item = orderItemFor(order, product, 2);
        item.setId(200L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.updateItemFulfillmentStatus(10L, 100L, 200L, ItemFulfillmentStatus.SHIPPED))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateItemFulfillmentStatusThrowsUnauthorizedWhenOrderNotYetPaid() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithIdAndStatus(100L, customer, OrderStatus.PENDING_PAYMENT);
        Product product = productOwnedBy(5L, 10L, 3.0);
        OrderItem item = orderItemFor(order, product, 2);
        item.setId(200L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.updateItemFulfillmentStatus(10L, 100L, 200L, ItemFulfillmentStatus.SHIPPED))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("This order has not been paid for yet");
    }

    @Test
    void updateItemFulfillmentStatusThrowsResourceNotFoundWhenOrderMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateItemFulfillmentStatus(10L, 404L, 200L, ItemFulfillmentStatus.SHIPPED))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItemFulfillmentStatusThrowsResourceNotFoundWhenItemMissing() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateItemFulfillmentStatus(10L, 100L, 999L, ItemFulfillmentStatus.SHIPPED))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItemFulfillmentStatusThrowsResourceNotFoundWhenItemBelongsToDifferentOrder() {
        Customer customer = customerWithIdAndName(1L, "Jane Doe");
        Order order = orderWithId(100L, customer);
        Order otherOrder = orderWithId(999L, customer);
        Product product = productOwnedBy(5L, 10L, 3.0);
        OrderItem item = orderItemFor(otherOrder, product, 2);
        item.setId(200L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> orderService.updateItemFulfillmentStatus(10L, 100L, 200L, ItemFulfillmentStatus.SHIPPED))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
