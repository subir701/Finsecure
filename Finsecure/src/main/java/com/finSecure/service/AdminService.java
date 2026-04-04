package com.finSecure.service;

import com.finSecure.dto.response.UserResponse;
import com.finSecure.entity.Role;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByStatus(Boolean isActive);
    UserResponse getUserById(UUID userId);
    String updateUserStatus(UUID userId, Boolean isActive);
    String updateUserRole(UUID userId, Role role);
    String deleteUser(UUID userId);
    String softDeleteUser(UUID userId);
}
