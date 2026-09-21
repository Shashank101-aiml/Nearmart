package com.buildit.service;

import com.buildit.dto.request.LoginRequest;
import com.buildit.dto.request.OtpRequestRequest;
import com.buildit.dto.request.OtpVerifyRequest;
import com.buildit.dto.request.RegisterRequest;
import com.buildit.dto.response.AuthResponse;
import com.buildit.dto.response.OtpRequestResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    OtpRequestResponse requestOtp(OtpRequestRequest request);
    AuthResponse verifyOtp(OtpVerifyRequest request);
}
