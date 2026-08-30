package com.buildit.service;

import com.buildit.dto.request.ProductRequest;
import com.buildit.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(Long vendorId, ProductRequest request);
    ProductResponse update(Long vendorId, Long productId, ProductRequest request);
    void delete(Long vendorId, Long productId);
    ProductResponse getById(Long productId);
    List<ProductResponse> listAvailable();
    List<ProductResponse> listByVendor(Long vendorId);
    List<ProductResponse> listOwnProducts(Long vendorId);
    ProductResponse setAvailability(Long productId, boolean available);
}
