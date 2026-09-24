package com.proga.auth_service.controller;

import com.proga.auth_service.dto.ApiResponse;
import com.proga.auth_service.dto.UserResponse;
import com.proga.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success(userService.searchUsers(query)));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }
}
