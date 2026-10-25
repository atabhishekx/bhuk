package com.restru.auth.dto;

/**
 * Current user profile response.
 */
public class UserProfileResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String status;
    private java.util.List<String> roles;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String email, String name, String phone,
                               String status, java.util.List<String> roles) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.util.List<String> getRoles() {
        return roles;
    }

    public void setRoles(java.util.List<String> roles) {
        this.roles = roles;
    }
}
