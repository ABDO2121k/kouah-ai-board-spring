package kouahai.springboot.project.service.auth.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kouahai.springboot.project.dto.auth.AuthResponse;
import kouahai.springboot.project.dto.auth.LoginRequest;
import kouahai.springboot.project.dto.auth.RegisterRequest;
import kouahai.springboot.project.exception.DuplicateResourceException;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.exception.VerificationException;
import kouahai.springboot.project.model.User;
import kouahai.springboot.project.model.UserStatus;
import kouahai.springboot.project.repository.UserRepository;
import kouahai.springboot.project.security.CustomUserDetails;
import kouahai.springboot.project.security.JwtService;
import kouahai.springboot.project.service.auth.AuthService;
import kouahai.springboot.project.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        validateUniqueUser(request);

        User user = createUser(request);
        User savedUser = userRepository.save(user);

        sendVerificationEmailAsync(savedUser);

        return buildRegistrationResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("User attempting to login: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Check if user has verified their email
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            log.warn("Login attempt by unverified user: {}", request.getUsername());
            throw new VerificationException(
                    "Please verify your email before logging in. Check your inbox for the verification link.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Login attempt by suspended user: {}", request.getUsername());
            throw new UnauthorizedException("Your account has been suspended. Please contact support.");
        }

        if (user.getStatus() == UserStatus.DELETED) {
            log.warn("Login attempt by deleted user: {}", request.getUsername());
            throw new UnauthorizedException("Your account has been deleted.");
        }

        String token = jwtService.generateToken(userDetails);

        log.info("User logged in successfully: {}", request.getUsername());

        return buildLoginResponse(token, userDetails);
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(String token) {
        log.info("Verifying email with token");

        boolean verified = emailService.verifyEmail(token);

        if (!verified) {
            throw new VerificationException("Invalid or expired verification token");
        }

        return AuthResponse.builder()
                .message("Email verified successfully. You can now login.")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new VerificationException("Email is already verified");
        }

        emailService.createVerificationToken(user);

        return AuthResponse.builder()
                .message("Verification email sent successfully.")
                .build();
    }

    // Private helper methods (Single Responsibility)

    private void validateUniqueUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
    }

    private User createUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        return user;
    }

    private void sendVerificationEmailAsync(User user) {
        try {
            emailService.createVerificationToken(user);
        } catch (Exception e) {
            log.error("Failed to send verification email for user: {}", user.getUsername(), e);
            // Don't fail registration if email fails bayna
        }
    }

    private AuthResponse buildRegistrationResponse(User user) {
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .message("User registered successfully. Please verify your email.")
                .build();
    }

    private AuthResponse buildLoginResponse(String token, CustomUserDetails userDetails) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .build();
    }
}
