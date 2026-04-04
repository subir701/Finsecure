package com.finSecure.service;

import com.finSecure.dto.response.UserResponse;
import com.finSecure.entity.Role;
import com.finSecure.entity.User;
import com.finSecure.exception.ApiException;
import com.finSecure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService{

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
        log.debug("Fetched {} users", users.size());
        return users;
    }

    @Override
    public List<UserResponse> getUsersByStatus(Boolean isActive) {
        log.debug("Fetching users with isActive={}", isActive);
        List<UserResponse> users = userRepository.findAllByIsActive(isActive).stream()
                .map(UserResponse ::from)
                .toList();
        return users;
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        log.debug("Fetching user by id: {}", userId);
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> {
                    log.warn("User not found: id={}", userId);
                    return ApiException.notFound("User not found with id: " + userId);
                });
    }

    @Transactional
    @Override
    public String updateUserStatus(UUID userId, Boolean isActive) {
        log.info("Updating user status: id={}, isActive={}", userId, isActive);
        int updated = userRepository.updateUserStatus(userId, isActive);
        if (updated == 0) throw ApiException.notFound("User not found with id: " + userId);
        log.info("User status updated: id={}, isActive={}", userId, isActive);
        return "User status updated to " + (isActive ? "active" : "inactive");
    }

    @Transactional
    @Override
    public String updateUserRole(UUID userId, Role role) {
        log.info("Updating user role: id={}, newRole={}", userId, role);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + userId));
        Role oldRole = user.getRole();
        user.setRole(role);
        userRepository.save(user);
        log.info("User role updated: id={}, {} -> {}", userId, oldRole, role);
        return "User role updated to " + role.name();
    }

    @Transactional
    @Override
    public String deleteUser(UUID userId) {
        log.info("Deleting user: id={}", userId);
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted: id={}", userId);
        return "User deleted successfully";
    }

    @Transactional
    @Override
    public String softDeleteUser(UUID userId) {
        log.info("Soft Deleting user: id={}", userId);
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("User not found with id: " + userId);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found with id: " + userId));
        user.setIsActive(false);

        userRepository.save(user);
        log.info("User soft deleted: id={}", userId);
        return "User is soft deleted successfully";
    }
}
