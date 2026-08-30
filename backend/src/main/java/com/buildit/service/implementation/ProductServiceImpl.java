package com.buildit.service.implementation;

import com.buildit.dto.request.ProductRequest;
import com.buildit.dto.response.ProductResponse;
import com.buildit.entity.Inventory;
import com.buildit.entity.Product;
import com.buildit.entity.Vendor;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.exception.UnauthorizedException;
import com.buildit.repository.InventoryRepository;
import com.buildit.repository.ProductRepository;
import com.buildit.repository.VendorRepository;
import com.buildit.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final InventoryRepository inventoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, VendorRepository vendorRepository,
                               InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.vendorRepository = vendorRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public ProductResponse create(Long vendorId, ProductRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        Product product = new Product();
        product.setVendor(vendor);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(request.getStockQuantity());
        inventoryRepository.save(inventory);

        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Long vendorId, Long productId, ProductRequest request) {
        Product product = findOwnedProduct(vendorId, productId);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        }
        product = productRepository.save(product);

        Inventory inventory = inventoryRepository.findByProductId(productId).orElseGet(() -> {
            Inventory newInventory = new Inventory();
            newInventory.setProductId(productId);
            return newInventory;
        });
        inventory.setQuantity(request.getStockQuantity());
        inventoryRepository.save(inventory);

        return toResponse(product);
    }

    @Override
    @Transactional
    public void delete(Long vendorId, Long productId) {
        Product product = findOwnedProduct(vendorId, productId);
        productRepository.delete(product);
    }

    @Override
    public ProductResponse getById(Long productId) {
        return toResponse(productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    @Override
    public List<ProductResponse> listAvailable() {
        return productRepository.findByAvailableTrue().stream().map(this::toResponse).toList();
    }

    @Override
    public List<ProductResponse> listByVendor(Long vendorId) {
        return productRepository.findByVendorIdAndAvailableTrue(vendorId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<ProductResponse> listOwnProducts(Long vendorId) {
        return productRepository.findByVendorId(vendorId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ProductResponse setAvailability(Long productId, boolean available) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setAvailable(available);
        product = productRepository.save(product);
        return toResponse(product);
    }

    private Product findOwnedProduct(Long vendorId, Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You do not own this product");
        }
        return product;
    }

    private ProductResponse toResponse(Product product) {
        int stockQuantity = inventoryRepository.findByProductId(product.getId())
            .map(inventory -> inventory.getQuantity() != null ? inventory.getQuantity() : 0)
            .orElse(0);

        return new ProductResponse(
            product.getId(),
            product.getTitle(),
            product.getDescription(),
            product.getPrice(),
            product.getAvailable(),
            product.getCreatedAt(),
            product.getVendor().getId(),
            product.getVendor().getStoreName(),
            stockQuantity
        );
    }
}
