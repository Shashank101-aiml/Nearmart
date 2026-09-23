package com.buildit.service;

import com.buildit.dto.response.DeliveryPartnerResponse;

public interface DeliveryPartnerService {
    DeliveryPartnerResponse getProfile(Long userId);
    DeliveryPartnerResponse setAvailability(Long userId, boolean available);
}
