package com.buildit.messaging.consumer;

import com.buildit.config.RabbitMQConfig;
import com.buildit.messaging.events.PaymentSuccessEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryConsumer {
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        // trigger delivery assignment
    }
}
