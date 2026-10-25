package com.restru.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Delivery assignment — links an order to a delivery partner.
 * Assignment state machine: UNASSIGNED -> OFFERED -> ASSIGNED -> ACCEPTED -> PICKED_UP -> COMPLETED
 * Terminal/exception: DECLINED, EXPIRED, FAILED, CANCELLED.
 * Maps to the {@code delivery_assignments} table.
 */
@Entity
@Table(name = "delivery_assignments")
@Getter
@Setter
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_assignments_order"))
    private com.restru.order.entity.Order order;

    @ManyToOne
    @JoinColumn(name = "partner_id", foreignKey = @ForeignKey(name = "fk_delivery_assignments_partner"))
    private DeliveryPartner partner;

    @Column(nullable = false, length = 30)
    private String state = "UNASSIGNED";

    @Column(name = "offered_at")
    private Instant offeredAt;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
