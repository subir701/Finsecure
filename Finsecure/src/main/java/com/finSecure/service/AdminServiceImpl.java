package com.finSecure.service;

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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByStatus(Boolean isActive) {
        return userRepository.findAllByIsActive(isActive);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + userId));
    }

    @Transactional
    public String updateUserStatus(UUID userId, Boolean isActive) {
        int updated = userRepository.updateUserStatus(userId, isActive);
        if (updated == 0) throw ApiException.notFound("User not found with id: " + userId);
        return "User status updated to " + (isActive ? "active" : "inactive");
    }

    @Transactional
    public String updateUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + userId));
        user.setRole(role);
        userRepository.save(user);
        return "User role updated to " + role.name();
    }

    @Transactional
    public String deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
        return "User deleted successfully";
    }
}
