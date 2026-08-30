package com.buildit.messaging.consumer;

import com.buildit.entity.Inventory;
import com.buildit.entity.OrderItem;
import com.buildit.entity.Product;
import com.buildit.messaging.events.OrderCreatedEvent;
import com.buildit.repository.InventoryRepository;
import com.buildit.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryConsumerTest {

    @Mock private OrderItemRepository orderItemRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryConsumer inventoryConsumer;

    private Product productWithId(Long id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }

    private OrderItem itemFor(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private Inventory inventoryOf(Long productId, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    @Test
    void decrementsInventoryCorrectlyForNormalLineItem() {
        Product product = productWithId(5L);
        OrderItem item = itemFor(product, 3);
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 10)));

        inventoryConsumer.handleOrderCreated(new OrderCreatedEvent(100L, 1L));

        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 7));
    }

    @Test
    void skipsLineItemWithNullProduct() {
        OrderItem item = new OrderItem();
        item.setProduct(null);
        item.setQuantity(1);
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));

        inventoryConsumer.handleOrderCreated(new OrderCreatedEvent(100L, 1L));

        verify(inventoryRepository, never()).findByProductId(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void skipsLineItemWithNoInventoryRow() {
        Product product = productWithId(5L);
        OrderItem item = itemFor(product, 1);
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.empty());

        inventoryConsumer.handleOrderCreated(new OrderCreatedEvent(100L, 1L));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void clampsAtZeroWhenQuantityWouldGoNegative() {
        Product product = productWithId(5L);
        OrderItem item = itemFor(product, 10);
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));
        when(inventoryRepository.findByProductId(5L)).thenReturn(Optional.of(inventoryOf(5L, 3)));

        inventoryConsumer.handleOrderCreated(new OrderCreatedEvent(100L, 1L));

        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 0));
    }
}
