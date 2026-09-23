package com.buildit.service.implementation;

import com.buildit.dto.request.LoginRequest;
import com.buildit.dto.request.OtpRequestRequest;
import com.buildit.dto.request.OtpVerifyRequest;
import com.buildit.dto.request.RegisterRequest;
import com.buildit.dto.response.AuthResponse;
import com.buildit.dto.response.OtpRequestResponse;
import com.buildit.entity.User;
import com.buildit.enums.UserRole;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.DuplicateResourceException;
import com.buildit.repository.CustomerRepository;
import com.buildit.repository.DeliveryPartnerRepository;
import com.buildit.repository.UserRepository;
import com.buildit.repository.VendorRepository;
import com.buildit.security.CustomUserDetails;
import com.buildit.security.JwtTokenProvider;
import com.buildit.service.OtpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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
    @Mock private DeliveryPartnerRepository deliveryPartnerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private OtpService otpService;

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
    void registerDeliveryPartnerSucceeds() {
        RegisterRequest request = baseRequest(UserRole.DELIVERY_PARTNER);
        request.setVehicleNumber("KA-01-AB-1234");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("token999");

        AuthResponse response = authService.register(request);

        assertThat(response.getRole()).isEqualTo("DELIVERY_PARTNER");
        verify(deliveryPartnerRepository).save(argThat(dp -> dp.getVehicleNumber().equals("KA-01-AB-1234")));
        verify(customerRepository, never()).save(any());
        verify(vendorRepository, never()).save(any());
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

    @Test
    void loginPropagatesDisabledException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("jdoe");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(DisabledException.class);
    }

    @Test
    void requestOtpSucceedsForLinkedCustomer() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setRole(UserRole.CUSTOMER);
        user.setPhoneNumber("9998887777");

        OtpRequestRequest request = new OtpRequestRequest();
        request.setPhoneNumber("9998887777");

        when(userRepository.findByPhoneNumber("9998887777")).thenReturn(Optional.of(user));
        when(otpService.generateAndStore("9998887777")).thenReturn("123456");

        OtpRequestResponse response = authService.requestOtp(request);

        assertThat(response.getOtp()).isEqualTo("123456");
    }

    @Test
    void requestOtpRejectsUnknownPhone() {
        OtpRequestRequest request = new OtpRequestRequest();
        request.setPhoneNumber("0000000000");
        when(userRepository.findByPhoneNumber("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.requestOtp(request))
            .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(otpService);
    }

    @Test
    void requestOtpRejectsNonCustomerRole() {
        User vendor = new User();
        vendor.setId(3L);
        vendor.setRole(UserRole.VENDOR);
        vendor.setPhoneNumber("9998887777");

        OtpRequestRequest request = new OtpRequestRequest();
        request.setPhoneNumber("9998887777");
        when(userRepository.findByPhoneNumber("9998887777")).thenReturn(Optional.of(vendor));

        assertThatThrownBy(() -> authService.requestOtp(request))
            .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(otpService);
    }

    @Test
    void verifyOtpSucceeds() {
        User user = new User();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setRole(UserRole.CUSTOMER);
        user.setPhoneNumber("9998887777");

        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setPhoneNumber("9998887777");
        request.setCode("123456");

        when(userRepository.findByPhoneNumber("9998887777")).thenReturn(Optional.of(user));
        when(otpService.verifyAndConsume("9998887777", "123456")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("otpToken");

        AuthResponse response = authService.verifyOtp(request);

        assertThat(response.getToken()).isEqualTo("otpToken");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void verifyOtpRejectsWrongCode() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.CUSTOMER);
        user.setPhoneNumber("9998887777");

        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setPhoneNumber("9998887777");
        request.setCode("000000");

        when(userRepository.findByPhoneNumber("9998887777")).thenReturn(Optional.of(user));
        when(otpService.verifyAndConsume("9998887777", "000000")).thenReturn(false);

        assertThatThrownBy(() -> authService.verifyOtp(request))
            .isInstanceOf(BadRequestException.class);

        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
