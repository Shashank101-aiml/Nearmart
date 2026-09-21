package com.buildit.controller;

import com.buildit.dto.request.LoginRequest;
import com.buildit.dto.request.OtpRequestRequest;
import com.buildit.dto.request.OtpVerifyRequest;
import com.buildit.dto.request.RegisterRequest;
import com.buildit.dto.response.AuthResponse;
import com.buildit.dto.response.OtpRequestResponse;
import com.buildit.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/request")
    public ResponseEntity<OtpRequestResponse> requestOtp(@Valid @RequestBody OtpRequestRequest request) {
        return ResponseEntity.ok(authService.requestOtp(request));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
}
