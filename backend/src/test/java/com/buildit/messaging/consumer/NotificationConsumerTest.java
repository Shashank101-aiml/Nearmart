package com.buildit.messaging.consumer;

import com.buildit.entity.Notification;
import com.buildit.messaging.events.NotificationEvent;
import com.buildit.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void receiveNotificationSavesANotificationForTheEventsCustomer() {
        notificationConsumer.receiveNotification(new NotificationEvent(7L, "Your order #100 has been placed."));

        verify(notificationRepository).save(argThat((Notification n) ->
            n.getCustomerId().equals(7L)
                && n.getMessage().equals("Your order #100 has been placed.")
                && !Boolean.TRUE.equals(n.getRead())));
    }
}
