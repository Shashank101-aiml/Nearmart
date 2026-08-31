package com.buildit.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTrackingHandlerTest {

    @Mock private WebSocketSession sessionForCustomerOne;
    @Mock private WebSocketSession sessionForCustomerTwo;

    private WebSocketSession sessionFor(Long customerId, WebSocketSession mockSession) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("customerId", customerId);
        lenient().when(mockSession.getAttributes()).thenReturn(attributes);
        return mockSession;
    }

    @Test
    void sendToCustomerOnlyReachesThatCustomersSessions() throws Exception {
        OrderTrackingHandler handler = new OrderTrackingHandler();
        WebSocketSession sessionOne = sessionFor(1L, sessionForCustomerOne);
        WebSocketSession sessionTwo = sessionFor(2L, sessionForCustomerTwo);
        when(sessionOne.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(sessionOne);
        handler.afterConnectionEstablished(sessionTwo);

        handler.sendToCustomer(1L, "{\"orderId\":100}");

        verify(sessionOne).sendMessage(new TextMessage("{\"orderId\":100}"));
        verify(sessionTwo, never()).sendMessage(any());
    }

    @Test
    void sendToCustomerWithNoConnectedSessionsDoesNothing() {
        OrderTrackingHandler handler = new OrderTrackingHandler();

        handler.sendToCustomer(99L, "{}");
    }

    @Test
    void closedSessionIsRemovedAndNoLongerReceivesMessages() throws Exception {
        OrderTrackingHandler handler = new OrderTrackingHandler();
        WebSocketSession sessionOne = sessionFor(1L, sessionForCustomerOne);

        handler.afterConnectionEstablished(sessionOne);
        handler.afterConnectionClosed(sessionOne, CloseStatus.NORMAL);

        handler.sendToCustomer(1L, "{}");

        verify(sessionOne, never()).sendMessage(any());
    }
}
