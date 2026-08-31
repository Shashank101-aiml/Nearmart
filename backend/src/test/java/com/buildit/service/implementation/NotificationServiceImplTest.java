package com.buildit.service.implementation;

import com.buildit.dto.response.NotificationResponse;
import com.buildit.entity.Notification;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.exception.UnauthorizedException;
import com.buildit.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notificationFor(Long id, Long customerId) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setCustomerId(customerId);
        notification.setMessage("Your order #100 has been placed.");
        return notification;
    }

    @Test
    void listForCustomerReturnsOnlyThatCustomersNotifications() {
        when(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of(notificationFor(50L, 1L)));

        List<NotificationResponse> responses = notificationService.listForCustomer(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(50L);
    }

    @Test
    void markReadSucceedsForOwningCustomer() {
        Notification notification = notificationFor(50L, 1L);
        when(notificationRepository.findById(50L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = notificationService.markRead(1L, 50L);

        assertThat(response.getRead()).isTrue();
    }

    @Test
    void markReadThrowsUnauthorizedWhenNotOwner() {
        when(notificationRepository.findById(50L)).thenReturn(Optional.of(notificationFor(50L, 1L)));

        assertThatThrownBy(() -> notificationService.markRead(2L, 50L))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void markReadThrowsResourceNotFoundWhenMissing() {
        when(notificationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(1L, 404L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
