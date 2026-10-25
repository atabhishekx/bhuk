package com.restru.auth.dto;

import java.util.List;

/**
 * Authentication response containing the JWT and the authenticated user's profile.
 */
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private UserProfile user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserProfile user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public static class UserProfile {
        private Long id;
        private String email;
        private String name;
        private String phone;
        private List<String> roles;

        public UserProfile() {
        }

        public UserProfile(Long id, String email, String name, String phone, List<String> roles) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.phone = phone;
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

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
