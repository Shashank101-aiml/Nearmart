package com.buildit.service.implementation;

import com.buildit.dto.request.LoginRequest;
import com.buildit.dto.request.RegisterRequest;
import com.buildit.dto.response.AuthResponse;
import com.buildit.entity.Customer;
import com.buildit.entity.User;
import com.buildit.entity.Vendor;
import com.buildit.enums.UserRole;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.DuplicateResourceException;
import com.buildit.repository.CustomerRepository;
import com.buildit.repository.UserRepository;
import com.buildit.repository.VendorRepository;
import com.buildit.security.CustomUserDetails;
import com.buildit.security.JwtTokenProvider;
import com.buildit.service.AuthService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                            CustomerRepository customerRepository,
                            VendorRepository vendorRepository,
                            PasswordEncoder passwordEncoder,
                            AuthenticationManager authenticationManager,
                            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Username or email is already registered");
        }

        if (request.getRole() == UserRole.CUSTOMER) {
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setName(request.getDisplayName());
            customer.setAddress(request.getAddress());
            customerRepository.save(customer);
        } else {
            Vendor vendor = new Vendor();
            vendor.setUser(user);
            vendor.setStoreName(request.getDisplayName());
            vendor.setLocation(request.getAddress());
            vendorRepository.save(vendor);
        }

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, userDetails.getUser().getId(), userDetails.getUsername(),
            userDetails.getUser().getRole().name());
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }
}
