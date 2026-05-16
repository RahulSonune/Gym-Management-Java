package com.fitlife.gym.web.dto;

import lombok.Data;

@Data
public class LoginRequest {

    /** FLENC1 JSON envelope {@code {email,password}} — preferred. */
    private String credentials;

    private String email;
    private String password;
}
