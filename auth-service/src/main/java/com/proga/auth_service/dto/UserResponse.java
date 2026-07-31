package com.proga.auth_service.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private long id;
    private String username;
    private String email;
    private Boolean isAdmin;
    private LocalDateTime createdAt;
}
