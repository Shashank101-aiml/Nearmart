package com.buildit.repository;

import com.buildit.entity.OrderItem;
import com.buildit.enums.ItemFulfillmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByProductVendorId(Long vendorId);
    List<OrderItem> findByFulfillmentStatusAndDeliveryPartnerIsNull(ItemFulfillmentStatus status);
    List<OrderItem> findByDeliveryPartnerId(Long deliveryPartnerId);
    boolean existsByDeliveryPartnerId(Long deliveryPartnerId);
}
