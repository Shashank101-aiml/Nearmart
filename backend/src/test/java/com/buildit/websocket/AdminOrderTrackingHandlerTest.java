package com.buildit.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderTrackingHandlerTest {

    @Mock private WebSocketSession sessionOne;
    @Mock private WebSocketSession sessionTwo;

    @Test
    void broadcastReachesAllConnectedSessions() throws Exception {
        AdminOrderTrackingHandler handler = new AdminOrderTrackingHandler();
        when(sessionOne.isOpen()).thenReturn(true);
        when(sessionTwo.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(sessionOne);
        handler.afterConnectionEstablished(sessionTwo);

        handler.broadcast("{\"orderId\":5}");

        verify(sessionOne).sendMessage(new TextMessage("{\"orderId\":5}"));
        verify(sessionTwo).sendMessage(new TextMessage("{\"orderId\":5}"));
    }

    @Test
    void closedSessionIsRemovedAndNoLongerReceivesBroadcasts() throws Exception {
        AdminOrderTrackingHandler handler = new AdminOrderTrackingHandler();

        handler.afterConnectionEstablished(sessionOne);
        handler.afterConnectionClosed(sessionOne, CloseStatus.NORMAL);

        handler.broadcast("{}");

        verify(sessionOne, never()).sendMessage(any());
    }
}
