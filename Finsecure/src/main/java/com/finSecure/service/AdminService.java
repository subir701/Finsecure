package com.finSecure.service;

import com.finSecure.entity.Role;
import com.finSecure.entity.User;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    List<User> getAllUsers();
    List<User> getUsersByStatus(Boolean isActive);
    User getUserById(UUID userId);
    String updateUserStatus(UUID userId, Boolean isActive);
    String updateUserRole(UUID userId, Role role);
    String deleteUser(UUID userId);
}
