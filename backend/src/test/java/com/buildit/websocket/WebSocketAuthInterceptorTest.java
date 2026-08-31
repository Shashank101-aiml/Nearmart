package com.buildit.websocket;

import com.buildit.entity.User;
import com.buildit.repository.UserRepository;
import com.buildit.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock private JwtTokenProvider tokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private ServerHttpResponse response;

    private ServerHttpRequest requestWithToken(String token) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        if (token != null) {
            servletRequest.setParameter("token", token);
        }
        return new ServletServerHttpRequest(servletRequest);
    }

    @Test
    void beforeHandshakeAcceptsValidTokenAndStoresCustomerId() {
        User user = new User();
        user.setId(7L);
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT("valid-token")).thenReturn("shopper");
        when(userRepository.findByUsername("shopper")).thenReturn(Optional.of(user));

        WebSocketAuthInterceptor interceptor = new WebSocketAuthInterceptor(tokenProvider, userRepository);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(requestWithToken("valid-token"), null, null, attributes);

        assertThat(accepted).isTrue();
        assertThat(attributes).containsEntry("customerId", 7L);
    }

    @Test
    void beforeHandshakeRejectsInvalidToken() {
        when(tokenProvider.validateToken("bad-token")).thenReturn(false);

        WebSocketAuthInterceptor interceptor = new WebSocketAuthInterceptor(tokenProvider, userRepository);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(requestWithToken("bad-token"), response, null, attributes);

        assertThat(accepted).isFalse();
        assertThat(attributes).isEmpty();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void beforeHandshakeRejectsMissingToken() {
        WebSocketAuthInterceptor interceptor = new WebSocketAuthInterceptor(tokenProvider, userRepository);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(requestWithToken(null), response, null, attributes);

        assertThat(accepted).isFalse();
        assertThat(attributes).isEmpty();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
