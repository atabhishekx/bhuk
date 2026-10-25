package com.restru.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Delivery issue / failed delivery report.
 * Maps to the {@code delivery_issues} table.
 */
@Entity
@Table(name = "delivery_issues")
@Getter
@Setter
public class DeliveryIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_issues_assignment"))
    private DeliveryAssignment assignment;

    @Column(nullable = false, length = 50, name = "issue_type")
    private String issueType;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant reportedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(length = 500)
    private String resolution;
}
