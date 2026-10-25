package com.restru.auth.controller;

import jakarta.validation.constraints.Size;

/**
 * Request to update user profile (name/phone).
 */
public class ProfileUpdateRequest {

    @Size(max = 150)
    private String name;

    @Size(max = 20)
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
