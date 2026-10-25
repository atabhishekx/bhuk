package com.restru.auth.controller;

import com.restru.auth.dto.AddressRequest;
import com.restru.auth.dto.AddressResponse;
import com.restru.auth.dto.UserProfileResponse;
import com.restru.auth.entity.User;
import com.restru.auth.entity.UserAddress;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.UserAddressRepository;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Current-user profile and address endpoints (protected).
 * GET    /api/v1/me                                    - current profile
 * PATCH  /api/v1/me                                    - update own profile
 * GET    /api/v1/me/addresses                          - list own addresses
 * POST   /api/v1/me/addresses                          - add address
 * PATCH  /api/v1/me/addresses/{addressId}              - update address
 * DELETE /api/v1/me/addresses/{addressId}              - delete address
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;

    public MeController(UserRepository userRepository,
                        UserRoleRepository userRoleRepository,
                        UserAddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails principal) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUsername()));

        List<String> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(new UserProfileResponse(
                user.getId(), user.getEmail(), user.getName(), user.getPhone(),
                user.getStatus(), roles
        )));
    }

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ProfileUpdateRequest request) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUsername()));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        user = userRepository.save(user);

        List<String> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(new UserProfileResponse(
                user.getId(), user.getEmail(), user.getName(), user.getPhone(),
                user.getStatus(), roles
        ), "Profile updated."));
    }

    @GetMapping("/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> listAddresses(
            @AuthenticationPrincipal UserDetails principal) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUsername()));

        List<AddressResponse> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::toAddressResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping("/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AddressRequest request) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUsername()));

        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setFullAddress(request.getFullAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLandmark(request.getLandmark());
        address.setPhone(request.getPhone());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId()).forEach(a -> {
                a.setIsDefault(false);
                addressRepository.save(a);
            });
        }

        address = addressRepository.save(address);

        return ResponseEntity
                .status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(toAddressResponse(address), "Address added."));
    }

    @PatchMapping("/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUsername()));

        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Address does not belong to this user");
        }

        address.setFullAddress(request.getFullAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLandmark(request.getLandmark());
        address.setPhone(request.getPhone());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId()).forEach(a -> {
                if (!a.getId().equals(addressId)) {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                }
            });
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        address = addressRepository.save(address);

        return ResponseEntity.ok(ApiResponse.success(toAddressResponse(address), "Address updated."));
    }

    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long addressId) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUsername()));

        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Address does not belong to this user");
        }

        addressRepository.delete(address);

        return ResponseEntity.ok(ApiResponse.ok("Address deleted."));
    }

    private AddressResponse toAddressResponse(UserAddress a) {
        return new AddressResponse(
                a.getId(), a.getFullAddress(), a.getCity(), a.getState(),
                a.getPincode(), a.getLandmark(), a.getPhone(),
                a.getIsDefault(), a.getCreatedAt(), a.getUpdatedAt()
        );
    }
}
