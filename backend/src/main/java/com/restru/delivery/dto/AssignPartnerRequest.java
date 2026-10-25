package com.restru.delivery.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for admin to manually assign/reassign a partner to a delivery.
 */
public class AssignPartnerRequest {

    @NotBlank
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
