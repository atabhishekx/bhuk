package com.restru.delivery.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to set partner availability.
 */
public class SetAvailabilityRequest {

    @NotBlank
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
