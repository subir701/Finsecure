package com.finSecure.controller;

import com.finSecure.dto.request.LoginRequest;
import com.finSecure.dto.request.UserRegisterRequest;
import com.finSecure.dto.response.LoginResponse;
import com.finSecure.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("finsecure/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Authentication", description = "User registration and JWT token acquisition")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user in the system. By default, all new users are assigned the 'VIEWER' role."
    )
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and receive JWT",
            description = "Verifies credentials and returns a stateless JWT token to be used in the 'Authorization' header for protected endpoints."
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
