package com.buildit.service.implementation;

import com.buildit.dto.response.NotificationResponse;
import com.buildit.entity.Notification;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.exception.UnauthorizedException;
import com.buildit.repository.NotificationRepository;
import com.buildit.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationResponse> listForCustomer(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long customerId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("You do not own this notification");
        }
        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getMessage(),
            notification.getRead(),
            notification.getCreatedAt()
        );
    }
}
