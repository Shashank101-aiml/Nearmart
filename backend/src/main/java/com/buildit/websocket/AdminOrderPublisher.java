package com.buildit.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdminOrderPublisher {
    private static final Logger log = LoggerFactory.getLogger(AdminOrderPublisher.class);

    private final AdminOrderTrackingHandler trackingHandler;
    private final ObjectMapper objectMapper;

    public AdminOrderPublisher(AdminOrderTrackingHandler trackingHandler, ObjectMapper objectMapper) {
        this.trackingHandler = trackingHandler;
        this.objectMapper = objectMapper;
    }

    public void broadcastOrderStatusChange(Long orderId, String status) {
        try {
            trackingHandler.broadcast(objectMapper.writeValueAsString(new AdminOrderUpdateMessage(orderId, status)));
        } catch (JsonProcessingException e) {
            log.error("Could not serialize admin order update for order '{}'.", orderId, e);
        }
    }
}
