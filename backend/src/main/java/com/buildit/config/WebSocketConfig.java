package com.buildit.config;
import com.buildit.websocket.OrderTrackingHandler;
import com.buildit.websocket.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final OrderTrackingHandler orderTrackingHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(OrderTrackingHandler orderTrackingHandler, WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.orderTrackingHandler = orderTrackingHandler;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderTrackingHandler, "/ws/tracking")
            .addInterceptors(webSocketAuthInterceptor)
            .setAllowedOrigins("*");
    }
}
