package com.finSecure.controller;

import com.finSecure.dto.response.UserResponse;
import com.finSecure.entity.Role;
import com.finSecure.entity.User;
import com.finSecure.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/finsecure/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "System-wide user management and role assignment (ADMIN only)")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(
            summary = "List all registered users",
            description = "Retrieves a list of all users in the system. Can be filtered by active/inactive status using the 'isActive' parameter."
    )
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) Boolean isActive) {
        if (isActive != null) return ResponseEntity.ok(adminService.getUsersByStatus(isActive));
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @Operation(
            summary = "Get user details by ID",
            description = "Fetches the full profile of a specific user, excluding sensitive credentials like passwords."
    )
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(
            summary = "Activate or Deactivate user account",
            description = "Toggles a user's access to the system. Deactivated users are blocked from logging in via the security filter."
    )
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID id,
            @RequestParam Boolean isActive) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, isActive));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(
            summary = "Promote or Demote user role",
            description = "Assigns a new role (VIEWER, ANALYST, ADMIN) to an existing user to change their system permissions."
    )
    public ResponseEntity<String> updateRole(
            @PathVariable UUID id,
            @RequestParam Role role) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    @DeleteMapping("/users/delete/{id}")
    @Operation(
            summary = "Soft delete a user",
            description = "Marks a user as deleted without removing their historical data from the database."
    )
    public ResponseEntity<String> softDelete(@PathVariable UUID id){
        return ResponseEntity.ok(adminService.softDeleteUser(id));
    }

    @DeleteMapping("/users/{id}")
    @Operation(
            summary = "Permanently delete a user",
            description = "Hard delete of a user account and all associated records from the database. This action is irreversible."
    )
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }
}