package com.buildit.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WebSocketPublisher {
    private static final Logger log = LoggerFactory.getLogger(WebSocketPublisher.class);

    private final OrderTrackingHandler trackingHandler;
    private final ObjectMapper objectMapper;

    public WebSocketPublisher(OrderTrackingHandler trackingHandler, ObjectMapper objectMapper) {
        this.trackingHandler = trackingHandler;
        this.objectMapper = objectMapper;
    }

    public void notifyCustomer(Long customerId, TrackingUpdateMessage message) {
        try {
            trackingHandler.sendToCustomer(customerId, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Could not serialize tracking update for customer '{}'.", customerId, e);
        }
    }
}
