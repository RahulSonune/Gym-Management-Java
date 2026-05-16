package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {
    String accessToken;
    String refreshToken;
    long expiresIn;
    AuthUserDto user;
}
