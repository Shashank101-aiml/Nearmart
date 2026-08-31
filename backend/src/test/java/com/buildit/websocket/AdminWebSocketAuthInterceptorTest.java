package com.buildit.websocket;

import com.buildit.entity.User;
import com.buildit.enums.UserRole;
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
class AdminWebSocketAuthInterceptorTest {

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

    private User userWithRole(UserRole role) {
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        return user;
    }

    @Test
    void beforeHandshakeAcceptsAdminToken() {
        when(tokenProvider.validateToken("admin-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT("admin-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(userWithRole(UserRole.ADMIN)));

        AdminWebSocketAuthInterceptor interceptor = new AdminWebSocketAuthInterceptor(tokenProvider, userRepository);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(requestWithToken("admin-token"), response, null, attributes);

        assertThat(accepted).isTrue();
    }

    @Test
    void beforeHandshakeRejectsNonAdminTokenWithForbidden() {
        when(tokenProvider.validateToken("customer-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT("customer-token")).thenReturn("shopper");
        when(userRepository.findByUsername("shopper")).thenReturn(Optional.of(userWithRole(UserRole.CUSTOMER)));

        AdminWebSocketAuthInterceptor interceptor = new AdminWebSocketAuthInterceptor(tokenProvider, userRepository);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(requestWithToken("customer-token"), response, null, attributes);

        assertThat(accepted).isFalse();
        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    void beforeHandshakeRejectsInvalidTokenWithUnauthorized() {
        when(tokenProvider.validateToken("bad-token")).thenReturn(false);

        AdminWebSocketAuthInterceptor interceptor = new AdminWebSocketAuthInterceptor(tokenProvider, userRepository);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(requestWithToken("bad-token"), response, null, attributes);

        assertThat(accepted).isFalse();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void beforeHandshakeRejectsMissingTokenWithUnauthorized() {
        AdminWebSocketAuthInterceptor interceptor = new AdminWebSocketAuthInterceptor(tokenProvider, userRepository);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(requestWithToken(null), response, null, attributes);

        assertThat(accepted).isFalse();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
