package com.buildit.websocket;

import com.buildit.entity.User;
import com.buildit.enums.UserRole;
import com.buildit.repository.UserRepository;
import com.buildit.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
public class AdminWebSocketAuthInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AdminWebSocketAuthInterceptor(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        String token = servletRequest.getServletRequest().getParameter("token");
        if (token == null || !tokenProvider.validateToken(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String username = tokenProvider.getUsernameFromJWT(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (user.get().getRole() != UserRole.ADMIN) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }
}
