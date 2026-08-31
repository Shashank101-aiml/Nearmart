package com.buildit.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class OrderTrackingHandler extends TextWebSocketHandler {
    private final Map<Long, CopyOnWriteArrayList<WebSocketSession>> sessionsByCustomerId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long customerId = customerIdFrom(session);
        if (customerId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessionsByCustomerId.computeIfAbsent(customerId, id -> new CopyOnWriteArrayList<>()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long customerId = customerIdFrom(session);
        if (customerId == null) {
            return;
        }
        sessionsByCustomerId.computeIfPresent(customerId, (id, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public void sendToCustomer(Long customerId, String message) {
        List<WebSocketSession> sessions = sessionsByCustomerId.get(customerId);
        if (sessions == null) {
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private Long customerIdFrom(WebSocketSession session) {
        Object customerId = session.getAttributes().get("customerId");
        return customerId instanceof Long ? (Long) customerId : null;
    }
}
