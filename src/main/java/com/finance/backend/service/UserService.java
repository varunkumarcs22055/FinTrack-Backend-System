package com.finance.backend.service;

import com.finance.backend.dto.user.UpdateRoleRequest;
import com.finance.backend.dto.user.UserResponse;
import com.finance.backend.entity.Role;
import com.finance.backend.entity.User;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User management service — ADMIN operations.
 *
 * Handles listing users, updating roles, toggling active status, and deletion.
 * All methods work with DTOs to avoid exposing entity internals.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * List all users (ADMIN only).
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single user by ID.
     */
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return toResponse(user);
    }

    /**
     * Update a user's role.
     * Validates the role string to provide a clean error message.
     */
    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User user = findUserOrThrow(id);

        try {
            Role newRole = Role.valueOf(request.getRole().toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: " + request.getRole() + ". Must be ADMIN, ANALYST, or VIEWER");
        }

        userRepository.save(user);
        return toResponse(user);
    }

    /**
     * Toggle user active/inactive status.
     * Inactive users cannot log in (enforced by UserDetails.isEnabled()).
     */
    public UserResponse toggleStatus(Long id) {
        User user = findUserOrThrow(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
        return toResponse(user);
    }

    /**
     * Delete a user permanently.
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ---- Private helpers ----

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
