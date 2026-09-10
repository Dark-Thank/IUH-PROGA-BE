package com.proga.auth_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String phoneNumber;
    private String fullName;
    private String displayName;
    private String jobTitle;
    private String bio;
    private Boolean isAdmin;
    private LocalDateTime createdAt;
}
