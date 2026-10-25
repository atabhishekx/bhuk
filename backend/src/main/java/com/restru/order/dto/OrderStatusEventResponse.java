package com.restru.order.dto;

import java.time.Instant;

/**
 * Order status transition audit event.
 */
public class OrderStatusEventResponse {

    private Long id;
    private String oldStatus;
    private String newStatus;
    private String actorEmail;
    private String actorRole;
    private String reason;
    private Instant createdAt;

    public OrderStatusEventResponse() {
    }

    public OrderStatusEventResponse(Long id, String oldStatus, String newStatus, String actorEmail,
                                    String actorRole, String reason, Instant createdAt) {
        this.id = id;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
