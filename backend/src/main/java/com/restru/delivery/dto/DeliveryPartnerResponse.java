package com.restru.delivery.dto;

import java.time.Instant;

/**
 * Delivery partner profile response.
 */
public class DeliveryPartnerResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long outletId;
    private String status;
    private java.math.BigDecimal currentLat;
    private java.math.BigDecimal currentLng;
    private Instant updatedAt;

    public DeliveryPartnerResponse() {
    }

    public DeliveryPartnerResponse(Long id, Long userId, String userEmail, String userName, Long outletId,
                                   String status, java.math.BigDecimal currentLat, java.math.BigDecimal currentLng,
                                   Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.outletId = outletId;
        this.status = status;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getOutletId() { return outletId; }
    public void setOutletId(Long outletId) { this.outletId = outletId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.math.BigDecimal getCurrentLat() { return currentLat; }
    public void setCurrentLat(java.math.BigDecimal currentLat) { this.currentLat = currentLat; }
    public java.math.BigDecimal getCurrentLng() { return currentLng; }
    public void setCurrentLng(java.math.BigDecimal currentLng) { this.currentLng = currentLng; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
