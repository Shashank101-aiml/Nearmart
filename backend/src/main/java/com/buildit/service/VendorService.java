package com.buildit.service;

import com.buildit.dto.response.VendorResponse;

public interface VendorService {
    VendorResponse getById(Long id);
}
