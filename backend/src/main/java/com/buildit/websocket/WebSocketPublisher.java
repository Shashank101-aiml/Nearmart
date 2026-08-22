package com.buildit.websocket;

import org.springframework.stereotype.Component;

@Component
public class WebSocketPublisher {
    private final OrderTrackingHandler trackingHandler;

    public WebSocketPublisher(OrderTrackingHandler trackingHandler) {
        this.trackingHandler = trackingHandler;
    }

    public void publishTrackingUpdate(String updateJson) {
        trackingHandler.broadcast(updateJson);
    }
}
