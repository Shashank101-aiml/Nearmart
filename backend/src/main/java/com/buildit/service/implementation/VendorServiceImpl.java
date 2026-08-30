package com.buildit.service.implementation;

import com.buildit.dto.response.VendorResponse;
import com.buildit.entity.Vendor;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.repository.VendorRepository;
import com.buildit.service.VendorService;
import org.springframework.stereotype.Service;

@Service
public class VendorServiceImpl implements VendorService {
    private final VendorRepository vendorRepository;

    public VendorServiceImpl(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    public VendorResponse getById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        return new VendorResponse(vendor.getId(), vendor.getStoreName(), vendor.getLocation());
    }
}
