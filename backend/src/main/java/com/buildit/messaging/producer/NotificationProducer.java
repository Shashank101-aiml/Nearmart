package com.buildit.messaging.producer;

import com.buildit.config.RabbitMQConfig;
import com.buildit.messaging.events.NotificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(Long customerId, String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "notification.send",
            new NotificationEvent(customerId, message));
    }
}
