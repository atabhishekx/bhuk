package com.restru.auth.service;

import com.restru.auth.dto.AuthRequest;
import com.restru.auth.dto.AuthResponse;
import com.restru.auth.dto.RegisterRequest;
import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.auth.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication business logic: registration and login.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        String role = request.getRole() != null ? request.getRole().toUpperCase() : "USER";
        if (!role.equals("USER") && !role.equals("DELIVERY_GUY")) {
            throw new IllegalArgumentException("Registration allowed only for USER or DELIVERY_GUY roles");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setOutlet(null);
        userRoleRepository.save(userRole);
        user.getRoles().add(userRole);

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getEmail()));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getPassword(),
                        user.getRoles().stream()
                                .map(ur -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + ur.getRole()))
                                .collect(java.util.stream.Collectors.toList())
                )
        );

        List<String> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        AuthResponse.UserProfile profile = new AuthResponse.UserProfile(
                user.getId(), user.getEmail(), user.getName(), user.getPhone(), roles);

        return new AuthResponse(token, profile);
    }
}
