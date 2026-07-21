package com.proga.auth_service.service;

import com.proga.auth_service.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(UUID id);
}
