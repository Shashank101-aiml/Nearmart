package com.buildit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> registerPayload(String username, String email, String role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("email", email);
        payload.put("password", "password123");
        payload.put("role", role);
        payload.put("displayName", "Test User");
        payload.put("address", "123 Main St");
        return payload;
    }

    @Test
    void registerCustomerReturnsCreatedWithValidToken() throws Exception {
        Map<String, Object> payload = registerPayload("customer1", "customer1@example.com", "CUSTOMER");

        String body = mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.role").value("CUSTOMER"))
            .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).get("token").asText();
        JsonNode claims = decodeJwtPayload(token);
        org.assertj.core.api.Assertions.assertThat(claims.get("role").asText()).isEqualTo("CUSTOMER");
        org.assertj.core.api.Assertions.assertThat(claims.has("userId")).isTrue();
    }

    @Test
    void registerDuplicateUsernameReturnsConflict() throws Exception {
        Map<String, Object> payload = registerPayload("dupuser", "dup1@example.com", "CUSTOMER");
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());

        Map<String, Object> duplicate = registerPayload("dupuser", "dup2@example.com", "VENDOR");
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(duplicate)))
            .andExpect(status().isConflict());
    }

    @Test
    void registerWithInvalidPayloadReturnsBadRequest() throws Exception {
        Map<String, Object> payload = registerPayload("baduser", "not-an-email", "CUSTOMER");
        payload.put("password", "short");

        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        Map<String, Object> payload = registerPayload("loginuser", "loginuser@example.com", "CUSTOMER");
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());

        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("username", "loginuser");
        loginPayload.put("password", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginPayload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        Map<String, Object> payload = registerPayload("wrongpass", "wrongpass@example.com", "CUSTOMER");
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());

        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("username", "wrongpass");
        loginPayload.put("password", "totallyWrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginPayload)))
            .andExpect(status().isUnauthorized());
    }

    private JsonNode decodeJwtPayload(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        return objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
    }
}
