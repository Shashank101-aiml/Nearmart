package com.buildit.service;

import com.buildit.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> listForCustomer(Long customerId);
    NotificationResponse markRead(Long customerId, Long notificationId);
}
