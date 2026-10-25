package com.restru.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Delivery location ping — real-time tracking breadcrumb.
 * Maps to the {@code delivery_tracking} table.
 */
@Entity
@Table(name = "delivery_tracking")
@Getter
@Setter
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_tracking_assignment"))
    private DeliveryAssignment assignment;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "accuracy")
    private Integer accuracy;

    @Column(name = "heading")
    private Integer heading;

    @Column(name = "speed")
    private Integer speed;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt = Instant.now();
}
