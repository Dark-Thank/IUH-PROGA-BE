package com.proga.auth_service.service;

import com.proga.auth_service.dto.AuthResponse;
import com.proga.auth_service.dto.LoginRequest;
import com.proga.auth_service.dto.RegisterRequest;
import com.proga.auth_service.dto.UserResponse;
import com.proga.auth_service.model.User;
import com.proga.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAdmin(false)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .isAdmin(savedUser.getIsAdmin())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username/email or password");
        }

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isAdmin(user.getIsAdmin())
                .createdAt(user.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .accessToken("dummy-jwt-token-placeholder")
                .tokenType("Bearer")
                .user(userResponse)
                .build();
    }
}
