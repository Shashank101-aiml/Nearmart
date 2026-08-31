package com.buildit.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InventoryPublisher {
    private static final Logger log = LoggerFactory.getLogger(InventoryPublisher.class);

    private final InventoryTrackingHandler trackingHandler;
    private final ObjectMapper objectMapper;

    public InventoryPublisher(InventoryTrackingHandler trackingHandler, ObjectMapper objectMapper) {
        this.trackingHandler = trackingHandler;
        this.objectMapper = objectMapper;
    }

    public void broadcastStockUpdate(Long productId, Integer stockQuantity) {
        try {
            trackingHandler.broadcast(objectMapper.writeValueAsString(new InventoryUpdateMessage(productId, stockQuantity)));
        } catch (JsonProcessingException e) {
            log.error("Could not serialize inventory update for product '{}'.", productId, e);
        }
    }
}
