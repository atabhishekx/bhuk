package com.restru.auth.service;

import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.UserAddressRepository;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.auth.dto.AddressResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User profile and address business logic.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;

    public UserService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       UserAddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    public com.restru.auth.dto.UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        List<String> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return new com.restru.auth.dto.UserProfileResponse(
                user.getId(), user.getEmail(), user.getName(), user.getPhone(),
                user.getStatus(), roles
        );
    }

    public List<AddressResponse> listAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId()).stream()
                .map(a -> new AddressResponse(
                        a.getId(), a.getFullAddress(), a.getCity(), a.getState(),
                        a.getPincode(), a.getLandmark(), a.getPhone(),
                        a.getIsDefault(), a.getCreatedAt(), a.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}
