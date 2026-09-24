package com.proga.auth_service.service;

import com.proga.auth_service.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();

    UserResponse getUserById(long id);

    List<UserResponse> searchUsers(String query);
}
