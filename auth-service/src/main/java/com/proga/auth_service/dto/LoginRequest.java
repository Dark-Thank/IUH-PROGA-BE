package com.proga.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username or Email cannot be blank")
    private String usernameOrEmail;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
