package com.buildit.service.implementation;

import com.buildit.dto.request.LoginRequest;
import com.buildit.dto.request.OtpRequestRequest;
import com.buildit.dto.request.OtpVerifyRequest;
import com.buildit.dto.request.RegisterRequest;
import com.buildit.dto.response.AuthResponse;
import com.buildit.dto.response.OtpRequestResponse;
import com.buildit.entity.Customer;
import com.buildit.entity.DeliveryPartner;
import com.buildit.entity.User;
import com.buildit.entity.Vendor;
import com.buildit.enums.UserRole;
import com.buildit.exception.BadRequestException;
import com.buildit.exception.DuplicateResourceException;
import com.buildit.repository.CustomerRepository;
import com.buildit.repository.DeliveryPartnerRepository;
import com.buildit.repository.UserRepository;
import com.buildit.repository.VendorRepository;
import com.buildit.security.CustomUserDetails;
import com.buildit.security.JwtTokenProvider;
import com.buildit.service.AuthService;
import com.buildit.service.OtpService;
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
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    public AuthServiceImpl(UserRepository userRepository,
                            CustomerRepository customerRepository,
                            VendorRepository vendorRepository,
                            DeliveryPartnerRepository deliveryPartnerRepository,
                            PasswordEncoder passwordEncoder,
                            AuthenticationManager authenticationManager,
                            JwtTokenProvider jwtTokenProvider,
                            OtpService otpService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.otpService = otpService;
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
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
            && userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Phone number is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhoneNumber(request.getPhoneNumber());

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
        } else if (request.getRole() == UserRole.VENDOR) {
            Vendor vendor = new Vendor();
            vendor.setUser(user);
            vendor.setStoreName(request.getDisplayName());
            vendor.setLocation(request.getAddress());
            vendorRepository.save(vendor);
        } else if (request.getRole() == UserRole.DELIVERY_PARTNER) {
            DeliveryPartner deliveryPartner = new DeliveryPartner();
            deliveryPartner.setUser(user);
            deliveryPartner.setName(request.getDisplayName());
            deliveryPartner.setVehicleNumber(request.getVehicleNumber());
            deliveryPartner.setAvailable(true);
            deliveryPartnerRepository.save(deliveryPartner);
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

    @Override
    public OtpRequestResponse requestOtp(OtpRequestRequest request) {
        User user = findOtpEligibleUser(request.getPhoneNumber());
        String code = otpService.generateAndStore(user.getPhoneNumber());
        return new OtpRequestResponse("OTP generated (simulated delivery, no SMS sent)", code);
    }

    @Override
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        User user = findOtpEligibleUser(request.getPhoneNumber());
        if (!otpService.verifyAndConsume(user.getPhoneNumber(), request.getCode())) {
            throw new BadRequestException("Invalid or expired OTP");
        }
        return buildAuthResponse(user);
    }

    private User findOtpEligibleUser(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new BadRequestException("No customer account found for this phone number"));
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new BadRequestException("No customer account found for this phone number");
        }
        return user;
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }
}
