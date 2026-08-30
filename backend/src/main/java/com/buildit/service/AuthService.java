package com.buildit.service;

import com.buildit.dto.request.LoginRequest;
import com.buildit.dto.request.RegisterRequest;
import com.buildit.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
