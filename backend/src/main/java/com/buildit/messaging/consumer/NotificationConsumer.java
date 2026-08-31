package com.buildit.messaging.consumer;

import com.buildit.config.RabbitMQConfig;
import com.buildit.entity.Notification;
import com.buildit.messaging.events.NotificationEvent;
import com.buildit.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private final NotificationRepository notificationRepository;

    public NotificationConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveNotification(NotificationEvent event) {
        Notification notification = new Notification();
        notification.setCustomerId(event.getCustomerId());
        notification.setMessage(event.getMessage());
        notificationRepository.save(notification);
    }
}
