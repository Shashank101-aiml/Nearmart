package com.buildit.service.implementation;

import com.buildit.dto.response.DeliveryPartnerResponse;
import com.buildit.entity.DeliveryPartner;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.repository.DeliveryPartnerRepository;
import com.buildit.service.DeliveryPartnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    public DeliveryPartnerServiceImpl(DeliveryPartnerRepository deliveryPartnerRepository) {
        this.deliveryPartnerRepository = deliveryPartnerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryPartnerResponse getProfile(Long userId) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
        return new DeliveryPartnerResponse(deliveryPartner.getId(), deliveryPartner.getName(),
            deliveryPartner.getVehicleNumber(), deliveryPartner.getAvailable());
    }

    @Override
    @Transactional
    public DeliveryPartnerResponse setAvailability(Long userId, boolean available) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
        deliveryPartner.setAvailable(available);
        deliveryPartner = deliveryPartnerRepository.save(deliveryPartner);

        return new DeliveryPartnerResponse(deliveryPartner.getId(), deliveryPartner.getName(),
            deliveryPartner.getVehicleNumber(), deliveryPartner.getAvailable());
    }
}
