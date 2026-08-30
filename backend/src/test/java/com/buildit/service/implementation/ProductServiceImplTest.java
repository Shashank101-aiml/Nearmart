package com.buildit.service.implementation;

import com.buildit.dto.request.ProductRequest;
import com.buildit.dto.response.ProductResponse;
import com.buildit.entity.Product;
import com.buildit.entity.Vendor;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.exception.UnauthorizedException;
import com.buildit.repository.ProductRepository;
import com.buildit.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private VendorRepository vendorRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Vendor vendorWithId(Long id) {
        Vendor vendor = new Vendor();
        vendor.setId(id);
        vendor.setStoreName("Acme Store");
        vendor.setLocation("123 Main St");
        return vendor;
    }

    private Product productOwnedBy(Long productId, Long vendorId) {
        Product product = new Product();
        product.setId(productId);
        product.setVendor(vendorWithId(vendorId));
        product.setTitle("Widget");
        product.setDescription("A useful widget");
        product.setPrice(9.99);
        product.setAvailable(true);
        return product;
    }

    private ProductRequest sampleRequest() {
        ProductRequest request = new ProductRequest();
        request.setTitle("Widget");
        request.setDescription("A useful widget");
        request.setPrice(9.99);
        return request;
    }

    @Test
    void createSucceedsForOwningVendor() {
        Vendor vendor = vendorWithId(1L);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        ProductResponse response = productService.create(1L, sampleRequest());

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getVendorId()).isEqualTo(1L);
        assertThat(response.getAvailable()).isTrue();
    }

    @Test
    void createThrowsResourceNotFoundWhenVendorMissing() {
        when(vendorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(99L, sampleRequest()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSucceedsWhenVendorOwnsProduct() {
        Product product = productOwnedBy(10L, 1L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = sampleRequest();
        request.setTitle("Updated Widget");

        ProductResponse response = productService.update(1L, 10L, request);

        assertThat(response.getTitle()).isEqualTo("Updated Widget");
    }

    @Test
    void updateThrowsUnauthorizedWhenVendorDoesNotOwnProduct() {
        Product product = productOwnedBy(10L, 1L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.update(2L, 10L, sampleRequest()))
            .isInstanceOf(UnauthorizedException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteThrowsUnauthorizedWhenVendorDoesNotOwnProduct() {
        Product product = productOwnedBy(10L, 1L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.delete(2L, 10L))
            .isInstanceOf(UnauthorizedException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void getByIdThrowsResourceNotFoundWhenMissing() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(404L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listAvailableReturnsOnlyAvailableProducts() {
        Product product = productOwnedBy(10L, 1L);
        when(productRepository.findByAvailableTrue()).thenReturn(List.of(product));

        List<ProductResponse> results = productService.listAvailable();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(10L);
        verify(productRepository).findByAvailableTrue();
    }
}
