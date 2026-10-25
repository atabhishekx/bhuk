package com.restru.delivery.entity;

import com.restru.auth.entity.Outlet;
import com.restru.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Delivery partner profile. Tracks availability and current location.
 * Maps to the {@code delivery_partners} table.
 */
@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_partners_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "outlet_id", foreignKey = @ForeignKey(name = "fk_delivery_partners_outlet"))
    private Outlet outlet;

    @Column(nullable = false, length = 30)
    private String status = "OFFLINE";

    @Column(name = "current_lat", precision = 10, scale = 7)
    private java.math.BigDecimal currentLat;

    @Column(name = "current_lng", precision = 10, scale = 7)
    private java.math.BigDecimal currentLng;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
