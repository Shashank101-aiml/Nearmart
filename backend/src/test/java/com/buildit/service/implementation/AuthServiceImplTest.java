package com.buildit.service.implementation;

import com.buildit.dto.request.LoginRequest;
import com.buildit.dto.request.RegisterRequest;
import com.buildit.dto.response.AuthResponse;
import com.buildit.entity.User;
import com.buildit.enums.UserRole;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.DuplicateResourceException;
import com.buildit.repository.CustomerRepository;
import com.buildit.repository.UserRepository;
import com.buildit.repository.VendorRepository;
import com.buildit.security.CustomUserDetails;
import com.buildit.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private VendorRepository vendorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest baseRequest(UserRole role) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("jdoe");
        request.setEmail("jdoe@example.com");
        request.setPassword("password123");
        request.setRole(role);
        request.setDisplayName("Jane Doe");
        request.setAddress("123 Main St");
        return request;
    }

    @Test
    void registerCustomerSucceeds() {
        RegisterRequest request = baseRequest(UserRole.CUSTOMER);
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("token123");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        verify(customerRepository).save(any());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    void registerVendorSucceeds() {
        RegisterRequest request = baseRequest(UserRole.VENDOR);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("token456");

        AuthResponse response = authService.register(request);

        assertThat(response.getRole()).isEqualTo("VENDOR");
        verify(vendorRepository).save(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerRejectsAdminRole() {
        RegisterRequest request = baseRequest(UserRole.ADMIN);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(userRepository, customerRepository, vendorRepository, jwtTokenProvider);
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequest request = baseRequest(UserRole.CUSTOMER);
        when(userRepository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = baseRequest(UserRole.CUSTOMER);
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginSucceeds() {
        LoginRequest request = new LoginRequest();
        request.setUsername("jdoe");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setRole(UserRole.CUSTOMER);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("token789");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token789");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void loginPropagatesBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("jdoe");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class);
    }
}
