package com.finSecure.service;

import com.finSecure.dto.request.LoginRequest;
import com.finSecure.dto.request.UserRegisterRequest;
import com.finSecure.dto.response.LoginResponse;

public interface AuthService {
    String register(UserRegisterRequest request);
    LoginResponse login(LoginRequest request);
}
