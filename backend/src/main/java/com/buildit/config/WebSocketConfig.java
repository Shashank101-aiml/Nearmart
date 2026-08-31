package com.buildit.config;
import com.buildit.websocket.AdminOrderTrackingHandler;
import com.buildit.websocket.AdminWebSocketAuthInterceptor;
import com.buildit.websocket.InventoryTrackingHandler;
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
    private final InventoryTrackingHandler inventoryTrackingHandler;
    private final AdminOrderTrackingHandler adminOrderTrackingHandler;
    private final AdminWebSocketAuthInterceptor adminWebSocketAuthInterceptor;

    public WebSocketConfig(OrderTrackingHandler orderTrackingHandler, WebSocketAuthInterceptor webSocketAuthInterceptor,
                            InventoryTrackingHandler inventoryTrackingHandler,
                            AdminOrderTrackingHandler adminOrderTrackingHandler,
                            AdminWebSocketAuthInterceptor adminWebSocketAuthInterceptor) {
        this.orderTrackingHandler = orderTrackingHandler;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        this.inventoryTrackingHandler = inventoryTrackingHandler;
        this.adminOrderTrackingHandler = adminOrderTrackingHandler;
        this.adminWebSocketAuthInterceptor = adminWebSocketAuthInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderTrackingHandler, "/ws/tracking")
            .addInterceptors(webSocketAuthInterceptor)
            .setAllowedOrigins("*");
        registry.addHandler(inventoryTrackingHandler, "/ws/inventory")
            .setAllowedOrigins("*");
        registry.addHandler(adminOrderTrackingHandler, "/ws/admin/orders")
            .addInterceptors(adminWebSocketAuthInterceptor)
            .setAllowedOrigins("*");
    }
}
