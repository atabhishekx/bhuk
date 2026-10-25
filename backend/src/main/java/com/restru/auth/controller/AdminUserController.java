package com.restru.auth.controller;

import com.restru.auth.dto.AssignRoleRequest;
import com.restru.auth.dto.UserProfileResponse;
import com.restru.auth.entity.Outlet;
import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.OutletRepository;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.auth.service.UserService;
import com.restru.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin user and role management.
 * Only ADMIN can assign/modify roles and outlet scoping.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OutletRepository outletRepository;
    private final UserService userService;

    public AdminUserController(UserRepository userRepository,
                               UserRoleRepository userRoleRepository,
                               OutletRepository outletRepository,
                               UserService userService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.outletRepository = outletRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> listUsers() {
        List<User> users = userRepository.findAll();
        List<UserProfileResponse> profiles = users.stream()
                .map(u -> userService.getProfile(u.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @PostMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserProfileResponse>> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String role = request.getRole().toUpperCase();
        if (!role.equals("USER") && !role.equals("DELIVERY_GUY") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        if (role.equals("DELIVERY_GUY") && request.getOutletId() == null) {
            throw new IllegalArgumentException("Outlet is required for DELIVERY_GUY role");
        }

        if ((role.equals("USER") || role.equals("ADMIN")) && request.getOutletId() != null) {
            throw new IllegalArgumentException(role + " role must not be scoped to an outlet");
        }

        Outlet outlet = null;
        if (request.getOutletId() != null) {
            outlet = outletRepository.findById(request.getOutletId())
                    .orElseThrow(() -> new IllegalArgumentException("Outlet not found: " + request.getOutletId()));
        }

        userRoleRepository.deleteByUser(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setOutlet(outlet);
        userRoleRepository.save(userRole);

        UserProfileResponse profile = userService.getProfile(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(profile, "Role assigned."));
    }
}
