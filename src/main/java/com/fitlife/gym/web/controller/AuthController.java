package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.AuthService;
import com.fitlife.gym.web.dto.AuthUserDto;
import com.fitlife.gym.web.dto.LoginRequest;
import com.fitlife.gym.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthUserDto me() {
        return authService.currentUser();
    }
}
