package com.finSecure.service;

import com.finSecure.dto.request.LoginRequest;
import com.finSecure.dto.request.UserRegisterRequest;
import com.finSecure.dto.response.LoginResponse;
import com.finSecure.entity.Role;
import com.finSecure.entity.User;
import com.finSecure.exception.ApiException;
import com.finSecure.repository.UserRepository;
import com.finSecure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    @Override
    public String register(UserRegisterRequest request) {

        log.info("Registration attempt for email: {}", request.email());

        if(userRepository.existsByEmail(request.email())){
            log.warn("Registration failed - email already exists: {}", request.email());
            throw ApiException.conflict("Email already registered");
        }

        Role assignedRole = userRepository.count() == 0 ? Role.ADMIN : Role.VIEWER;

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(assignedRole)
                .build();

        userRepository.save(user);

        log.info("User registered successfully: email={}, role={}", user.getEmail(), user.getRole());

        return "Registration Successful";
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.notFound("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        log.info("Login successful: email={}, role={}", user.getEmail(), user.getRole());

        return new LoginResponse(token,user.getEmail(),user.getRole().name(),"Login Successful");
    }


}
