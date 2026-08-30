package com.buildit.messaging.consumer;

import com.buildit.config.RabbitMQConfig;
import com.buildit.entity.OrderItem;
import com.buildit.messaging.events.OrderCreatedEvent;
import com.buildit.repository.InventoryRepository;
import com.buildit.repository.OrderItemRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InventoryConsumer {
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;

    public InventoryConsumer(OrderItemRepository orderItemRepository, InventoryRepository inventoryRepository) {
        this.orderItemRepository = orderItemRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Decrements inventory asynchronously after an order is placed. Stock is only
     * re-validated synchronously at checkout (see OrderServiceImpl.placeOrder) — the
     * actual decrement happening here, after the message is consumed, means two
     * concurrent checkouts could both pass that validation before either decrements.
     * Deliberate simplification: no reservation/locking in this slice.
     */
    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        List<OrderItem> items = orderItemRepository.findByOrderId(event.getOrderId());
        for (OrderItem item : items) {
            if (item.getProduct() == null) {
                continue;
            }
            inventoryRepository.findByProductId(item.getProduct().getId()).ifPresent(inventory -> {
                int updated = Math.max(0, inventory.getQuantity() - item.getQuantity());
                inventory.setQuantity(updated);
                inventoryRepository.save(inventory);
            });
        }
    }
}
