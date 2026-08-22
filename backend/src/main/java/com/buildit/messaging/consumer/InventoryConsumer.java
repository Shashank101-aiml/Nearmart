package com.buildit.messaging.consumer;

import com.buildit.config.RabbitMQConfig;
import com.buildit.messaging.events.OrderCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {
    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        // deduct inventory
    }
}
