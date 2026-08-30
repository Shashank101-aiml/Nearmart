package com.buildit.service.implementation;

import com.buildit.dto.request.AddCartItemRequest;
import com.buildit.dto.request.UpdateCartItemRequest;
import com.buildit.dto.response.CartItemResponse;
import com.buildit.dto.response.CartResponse;
import com.buildit.entity.Cart;
import com.buildit.entity.CartItem;
import com.buildit.entity.Customer;
import com.buildit.entity.Product;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.repository.CartItemRepository;
import com.buildit.repository.CartRepository;
import com.buildit.repository.CustomerRepository;
import com.buildit.repository.InventoryRepository;
import com.buildit.repository.ProductRepository;
import com.buildit.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public CartServiceImpl(CartRepository cartRepository,
                            CartItemRepository cartItemRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository,
                            InventoryRepository inventoryRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public CartResponse getCart(Long customerId) {
        return toResponse(getOrCreateCart(customerId));
    }

    @Override
    @Transactional
    public CartResponse addItem(Long customerId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(0);
                return newItem;
            });

        int newQuantity = item.getQuantity() + request.getQuantity();
        int stock = currentStock(product.getId());
        if (newQuantity > stock) {
            throw new BadRequestException("Only " + stock + " unit(s) of \"" + product.getTitle() + "\" available");
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long customerId, Long productId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"));

        int stock = currentStock(productId);
        if (request.getQuantity() > stock) {
            throw new BadRequestException("Only " + stock + " unit(s) of \"" + item.getProduct().getTitle() + "\" available");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long customerId, Long productId) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"));

        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    private Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            Cart cart = new Cart();
            cart.setCustomer(customer);
            return cartRepository.save(cart);
        });
    }

    private int currentStock(Long productId) {
        return inventoryRepository.findByProductId(productId)
            .map(inventory -> inventory.getQuantity() != null ? inventory.getQuantity() : 0)
            .orElse(0);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = items.stream().map(item -> {
            Product product = item.getProduct();
            double lineTotal = product.getPrice() * item.getQuantity();
            return new CartItemResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                item.getQuantity(),
                lineTotal,
                currentStock(product.getId())
            );
        }).toList();

        double total = itemResponses.stream().mapToDouble(CartItemResponse::getLineTotal).sum();
        return new CartResponse(cart.getId(), itemResponses, total);
    }
}
