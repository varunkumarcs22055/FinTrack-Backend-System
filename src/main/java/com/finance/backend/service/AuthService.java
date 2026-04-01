package com.finance.backend.service;

import com.finance.backend.config.JwtService;
import com.finance.backend.dto.auth.AuthResponse;
import com.finance.backend.dto.auth.LoginRequest;
import com.finance.backend.dto.auth.RegisterRequest;
import com.finance.backend.entity.Role;
import com.finance.backend.entity.User;
import com.finance.backend.exception.DuplicateResourceException;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service — handles registration and login.
 *
 * Design decisions:
 * - New users always get VIEWER role (principle of least privilege)
 * - Login uses Spring's AuthenticationManager for proper credential checking
 * - Returns JWT in the response so frontend can store and use it
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user with VIEWER role.
     * Validates uniqueness of username and email before creating.
     */
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Build and save user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.VIEWER) // Default role — least privilege
                .build();

        userRepository.save(user);

        // Generate token and return
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    /**
     * Authenticate user and return JWT.
     * Spring's AuthenticationManager handles:
     * - Password verification (BCrypt)
     * - Account status checks (active/inactive via UserDetails.isEnabled())
     */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (DisabledException e) {
            throw new DisabledException("Account is deactivated. Contact an administrator.");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(); // Safe — authentication passed

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
