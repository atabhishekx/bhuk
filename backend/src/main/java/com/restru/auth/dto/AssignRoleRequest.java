package com.restru.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to assign or update a user's role.
 * ADMIN can assign USER, DELIVERY_GUY, or ADMIN roles, optionally scoped to an outlet.
 */
public class AssignRoleRequest {

    @NotBlank
    private String role;

    @NotNull
    private Long outletId; // null for USER/ADMIN (no outlet scoping), required for DELIVERY_GUY

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getOutletId() {
        return outletId;
    }

    public void setOutletId(Long outletId) {
        this.outletId = outletId;
    }
}
