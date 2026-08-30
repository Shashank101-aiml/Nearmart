package com.buildit.service.implementation;

import com.buildit.dto.request.AddCartItemRequest;
import com.buildit.dto.request.UpdateCartItemRequest;
import com.buildit.dto.response.CartResponse;
import com.buildit.entity.Cart;
import com.buildit.entity.CartItem;
import com.buildit.entity.Customer;
import com.buildit.entity.Inventory;
import com.buildit.entity.Product;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.ResourceNotFoundException;
import com.buildit.repository.CartItemRepository;
import com.buildit.repository.CartRepository;
import com.buildit.repository.CustomerRepository;
import com.buildit.repository.InventoryRepository;
import com.buildit.repository.ProductRepository;
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
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cartWithId(Long id) {
        Cart cart = new Cart();
        cart.setId(id);
        Customer customer = new Customer();
        customer.setId(1L);
        cart.setCustomer(customer);
        return cart;
    }

    private Product productWithId(Long id, double price) {
        Product product = new Product();
        product.setId(id);
        product.setTitle("Widget");
        product.setPrice(price);
        return product;
    }

    private CartItem cartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(50L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void getCartCreatesCartWhenNoneExists() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());
        Customer customer = new Customer();
        customer.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Cart savedCart = cartWithId(10L);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of());

        CartResponse response = cartService.getCart(1L);

        assertThat(response.getCartId()).isEqualTo(10L);
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotal()).isEqualTo(0.0);
    }

    @Test
    void getCartReturnsExistingCartWithCorrectTotal() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        CartItem item = cartItem(cart, product, 2);
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(item));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 10)));

        CartResponse response = cartService.getCart(1L);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(7.0);
    }

    private Inventory inventoryOf(Long productId, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    @Test
    void addItemCreatesNewLineWhenNotPresent() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 10)));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(cartItem(cart, product, 2)));

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(5L);
        request.setQuantity(2);

        CartResponse response = cartService.addItem(1L, request);

        assertThat(response.getItems()).hasSize(1);
        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 2));
    }

    @Test
    void addItemIncrementsExistingLine() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        CartItem existing = cartItem(cart, product, 2);
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 10)));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(existing));

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(5L);
        request.setQuantity(3);

        cartService.addItem(1L, request);

        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 5));
    }

    @Test
    void addItemThrowsBadRequestWhenExceedsStock() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 2)));

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(5L);
        request.setQuantity(5);

        assertThatThrownBy(() -> cartService.addItem(1L, request))
            .isInstanceOf(BadRequestException.class);

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItemTreatsMissingInventoryAsZeroStock() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.empty());

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(5L);
        request.setQuantity(1);

        assertThatThrownBy(() -> cartService.addItem(1L, request))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addItemThrowsResourceNotFoundWhenProductMissing() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(99L);
        request.setQuantity(1);

        assertThatThrownBy(() -> cartService.addItem(1L, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItemQuantitySucceedsWithinStock() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        CartItem existing = cartItem(cart, product, 2);
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 10)));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(existing));

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(4);

        cartService.updateItemQuantity(1L, 5L, request);

        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 4));
    }

    @Test
    void updateItemQuantityThrowsBadRequestWhenExceedsStock() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        CartItem existing = cartItem(cart, product, 2);
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 3)));

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(10);

        assertThatThrownBy(() -> cartService.updateItemQuantity(1L, 5L, request))
            .isInstanceOf(BadRequestException.class);

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateItemQuantityThrowsResourceNotFoundWhenItemNotInCart() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(1);

        assertThatThrownBy(() -> cartService.updateItemQuantity(1L, 5L, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItemDeletesExistingLine() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        Product product = productWithId(5L, 3.5);
        CartItem existing = cartItem(cart, product, 2);
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of());

        cartService.removeItem(1L, 5L);

        verify(cartItemRepository).delete(existing);
    }

    @Test
    void removeItemThrowsResourceNotFoundWhenItemNotInCart() {
        Cart cart = cartWithId(10L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(1L, 5L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
