package com.proga.auth_service.service;

import com.proga.auth_service.dto.AuthResponse;
import com.proga.auth_service.dto.LoginRequest;
import com.proga.auth_service.dto.RegisterRequest;
import com.proga.auth_service.dto.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
