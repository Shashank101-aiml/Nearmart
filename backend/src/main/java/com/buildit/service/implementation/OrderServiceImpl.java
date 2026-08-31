package com.buildit.service.implementation;

import com.buildit.dto.request.VerifyPaymentRequest;
import com.buildit.dto.response.OrderItemResponse;
import com.buildit.dto.response.OrderResponse;
import com.buildit.dto.response.VendorOrderResponse;
import com.buildit.entity.Cart;
import com.buildit.entity.CartItem;
import com.buildit.entity.Order;
import com.buildit.entity.OrderItem;
import com.buildit.entity.Payment;
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
import com.buildit.service.OrderService;
import com.buildit.service.RazorpayGateway;
import com.buildit.service.RazorpayOrderResult;
import com.buildit.websocket.TrackingUpdateMessage;
import com.buildit.websocket.WebSocketPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderProducer orderProducer;
    private final RazorpayGateway razorpayGateway;
    private final PaymentRepository paymentRepository;
    private final WebSocketPublisher webSocketPublisher;
    private final String razorpayKeyId;

    public OrderServiceImpl(OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository,
                             CartRepository cartRepository,
                             CartItemRepository cartItemRepository,
                             InventoryRepository inventoryRepository,
                             OrderProducer orderProducer,
                             RazorpayGateway razorpayGateway,
                             PaymentRepository paymentRepository,
                             WebSocketPublisher webSocketPublisher,
                             @Value("${razorpay.key-id}") String razorpayKeyId) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderProducer = orderProducer;
        this.razorpayGateway = razorpayGateway;
        this.paymentRepository = paymentRepository;
        this.webSocketPublisher = webSocketPublisher;
        this.razorpayKeyId = razorpayKeyId;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new BadRequestException("Cart is empty"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        for (CartItem cartItem : cartItems) {
            int stock = currentStock(cartItem.getProduct().getId());
            if (cartItem.getQuantity() > stock) {
                throw new BadRequestException(
                    "Only " + stock + " unit(s) of \"" + cartItem.getProduct().getTitle() + "\" available");
            }
        }

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductTitle(cartItem.getProduct().getTitle());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItemRepository.save(orderItem));
        }

        cartItemRepository.deleteAll(cartItems);

        double total = orderItems.stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();
        RazorpayOrderResult razorpayOrder = razorpayGateway.createOrder(Math.round(total * 100), "order_" + order.getId());

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setRazorpayOrderId(razorpayOrder.getRazorpayOrderId());
        payment.setAmount(total);
        payment.setStatus(PaymentStatus.CREATED);
        payment = paymentRepository.save(payment);

        return toResponseWithPaymentLaunch(order, orderItems, payment);
    }

    @Override
    public List<OrderResponse> listOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public OrderResponse getOrder(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You do not own this order");
        }
        return toResponse(order);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public OrderResponse verifyPayment(Long customerId, Long orderId, VerifyPaymentRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You do not own this order");
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found for this order"));

        boolean valid = razorpayGateway.verifySignature(
            request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            throw new BadRequestException("Payment verification failed");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        order.setStatus(OrderStatus.PLACED);
        order = orderRepository.save(order);

        // Publish only after the transaction commits — the InventoryConsumer looks up this
        // order's items by id, and if the message were published before commit, RabbitMQ could
        // deliver it to the consumer faster than the DB commit finishes, making those items
        // briefly invisible to the consumer's own query. (Moved here from placeOrder — stock is
        // now only decremented once payment is actually verified, not at order creation.)
        Long placedOrderId = order.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderProducer.sendOrderCreatedEvent(new OrderCreatedEvent(placedOrderId, customerId));
            }
        });

        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse retryPayment(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You do not own this order");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new BadRequestException("Order is not awaiting payment");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found for this order"));

        RazorpayOrderResult razorpayOrder = razorpayGateway.createOrder(
            Math.round(payment.getAmount() * 100), "order_" + orderId + "_retry_" + System.currentTimeMillis());
        payment.setRazorpayOrderId(razorpayOrder.getRazorpayOrderId());
        payment.setRazorpayPaymentId(null);
        payment.setStatus(PaymentStatus.CREATED);
        payment = paymentRepository.save(payment);

        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order = orderRepository.save(order);
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toResponseWithPaymentLaunch(order, items, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorOrderResponse> listVendorOrders(Long vendorId) {
        List<OrderItem> items = orderItemRepository.findByProductVendorId(vendorId);

        Map<Long, List<OrderItem>> byOrderId = new LinkedHashMap<>();
        for (OrderItem item : items) {
            byOrderId.computeIfAbsent(item.getOrder().getId(), key -> new ArrayList<>()).add(item);
        }

        List<VendorOrderResponse> responses = new ArrayList<>();
        for (List<OrderItem> group : byOrderId.values()) {
            if (group.get(0).getOrder().getStatus() != OrderStatus.PLACED) {
                continue;
            }
            responses.add(toVendorOrderResponse(group.get(0).getOrder(), group));
        }

        responses.sort(Comparator.comparing(VendorOrderResponse::getCreatedAt).reversed());
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public VendorOrderResponse getVendorOrder(Long vendorId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> ownedItems = orderItemRepository.findByOrderId(orderId).stream()
            .filter(item -> item.getProduct() != null && item.getProduct().getVendor().getId().equals(vendorId))
            .toList();

        if (ownedItems.isEmpty()) {
            throw new UnauthorizedException("You have no items in this order");
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new UnauthorizedException("This order has not been paid for yet");
        }

        return toVendorOrderResponse(order, ownedItems);
    }

    @Override
    @Transactional
    public VendorOrderResponse updateItemFulfillmentStatus(Long vendorId, Long orderId, Long itemId,
                                                             ItemFulfillmentStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        if (!item.getOrder().getId().equals(orderId)) {
            throw new ResourceNotFoundException("Order item not found");
        }
        if (item.getProduct() == null || !item.getProduct().getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You do not own this item");
        }
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new UnauthorizedException("This order has not been paid for yet");
        }
        if (newStatus.ordinal() <= item.getFulfillmentStatus().ordinal()) {
            throw new BadRequestException(
                "Cannot move fulfillment status from " + item.getFulfillmentStatus() + " to " + newStatus);
        }

        item.setFulfillmentStatus(newStatus);
        orderItemRepository.save(item);

        webSocketPublisher.notifyCustomer(order.getCustomer().getId(),
            new TrackingUpdateMessage(orderId, itemId, newStatus.name()));

        List<OrderItem> ownedItems = orderItemRepository.findByOrderId(orderId).stream()
            .filter(i -> i.getProduct() != null && i.getProduct().getVendor().getId().equals(vendorId))
            .toList();
        return toVendorOrderResponse(order, ownedItems);
    }

    private VendorOrderResponse toVendorOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = toItemResponses(items);

        double vendorSubtotal = itemResponses.stream().mapToDouble(OrderItemResponse::getLineTotal).sum();

        return new VendorOrderResponse(
            order.getId(),
            order.getStatus().name(),
            order.getCreatedAt(),
            order.getCustomer().getName(),
            itemResponses,
            vendorSubtotal
        );
    }

    private int currentStock(Long productId) {
        return inventoryRepository.findByProductId(productId)
            .map(inventory -> inventory.getQuantity() != null ? inventory.getQuantity() : 0)
            .orElse(0);
    }

    private List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
            .map(item -> new OrderItemResponse(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice() * item.getQuantity(),
                item.getFulfillmentStatus().name()
            ))
            .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = toItemResponses(orderItemRepository.findByOrderId(order.getId()));
        double total = itemResponses.stream().mapToDouble(OrderItemResponse::getLineTotal).sum();

        return new OrderResponse(order.getId(), order.getStatus().name(), order.getCreatedAt(), itemResponses,
            total, null, null, null);
    }

    private OrderResponse toResponseWithPaymentLaunch(Order order, List<OrderItem> items, Payment payment) {
        List<OrderItemResponse> itemResponses = toItemResponses(items);
        double total = itemResponses.stream().mapToDouble(OrderItemResponse::getLineTotal).sum();
        long amountInPaise = Math.round(payment.getAmount() * 100);

        return new OrderResponse(order.getId(), order.getStatus().name(), order.getCreatedAt(), itemResponses,
            total, payment.getRazorpayOrderId(), razorpayKeyId, amountInPaise);
    }
}
