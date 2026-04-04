package com.finSecure.controller;

import com.finSecure.dto.response.UserResponse;
import com.finSecure.entity.Role;
import com.finSecure.entity.User;
import com.finSecure.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/users
     * List all users. Optional filter: ?isActive=true|false
     */
    @GetMapping("/users")
    @Operation(summary = "List all users. Filter by ?isActive=true|false")
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) Boolean isActive) {
        if (isActive != null) return ResponseEntity.ok(adminService.getUsersByStatus(isActive));
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }


    /**
     * PATCH /api/admin/users/{id}/status?isActive=true|false
     * Activate or deactivate a user account.
     */
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID id,
            @RequestParam Boolean isActive) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, isActive));
    }

    /**
     * PATCH /api/admin/users/{id}/role?role=VIEWER|ANALYST|ADMIN
     * Promote or demote a user's role.
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<String> updateRole(
            @PathVariable UUID id,
            @RequestParam Role role) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    @PatchMapping("/users/delete/{id}")
    public ResponseEntity<String> softDelete(@PathVariable UUID id){
        return ResponseEntity.ok(adminService.softDeleteUser(id));
    }
    /**
     * DELETE /api/admin/users/{id}
     * Permanently delete a user and their records (cascaded via DB).
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }
}