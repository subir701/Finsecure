package com.finSecure.dto.response;

public record LoginResponse(
        String token,
        String email,
        String role,
        String message
) {
}
