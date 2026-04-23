package kouahai.springboot.project.service.auth;

import kouahai.springboot.project.dto.auth.AuthResponse;
import kouahai.springboot.project.dto.auth.LoginRequest;
import kouahai.springboot.project.dto.auth.RegisterRequest;

/**
 * Service for authentication operations
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse verifyEmail(String token);

    AuthResponse resendVerificationEmail(String email);
}
