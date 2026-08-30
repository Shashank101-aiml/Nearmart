package com.buildit.service.implementation;

import com.buildit.dto.response.OrderItemResponse;
import com.buildit.dto.response.OrderResponse;
import com.buildit.dto.response.VendorOrderResponse;
import com.buildit.entity.Cart;
import com.buildit.entity.CartItem;
import com.buildit.entity.Order;
import com.buildit.entity.OrderItem;
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
import com.buildit.service.OrderService;
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

    public OrderServiceImpl(OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository,
                             CartRepository cartRepository,
                             CartItemRepository cartItemRepository,
                             InventoryRepository inventoryRepository,
                             OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderProducer = orderProducer;
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
        order.setStatus(OrderStatus.PLACED);
        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductTitle(cartItem.getProduct().getTitle());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItemRepository.save(orderItem);
        }

        cartItemRepository.deleteAll(cartItems);

        // Publish only after the transaction commits — the InventoryConsumer looks up this
        // order's items by id, and if the message were published before commit, RabbitMQ could
        // deliver it to the consumer faster than the DB commit finishes, making those items
        // briefly invisible to the consumer's own query.
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
    @Transactional(readOnly = true)
    public List<VendorOrderResponse> listVendorOrders(Long vendorId) {
        List<OrderItem> items = orderItemRepository.findByProductVendorId(vendorId);

        Map<Long, List<OrderItem>> byOrderId = new LinkedHashMap<>();
        for (OrderItem item : items) {
            byOrderId.computeIfAbsent(item.getOrder().getId(), key -> new ArrayList<>()).add(item);
        }

        List<VendorOrderResponse> responses = new ArrayList<>();
        for (List<OrderItem> group : byOrderId.values()) {
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

        return toVendorOrderResponse(order, ownedItems);
    }

    private VendorOrderResponse toVendorOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
            .map(item -> new OrderItemResponse(
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice() * item.getQuantity()
            ))
            .toList();

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

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = orderItemRepository.findByOrderId(order.getId()).stream()
            .map(item -> new OrderItemResponse(
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice() * item.getQuantity()
            ))
            .toList();

        double total = itemResponses.stream().mapToDouble(OrderItemResponse::getLineTotal).sum();

        return new OrderResponse(order.getId(), order.getStatus().name(), order.getCreatedAt(), itemResponses, total);
    }
}
