package com.proga.auth_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserProfileRequest {
    private String fullName;
    private String displayName;
    private String phoneNumber;
    private String jobTitle;
    private String bio;
    private String avatarUrl;
}
