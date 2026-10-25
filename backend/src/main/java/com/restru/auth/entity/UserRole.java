package com.restru.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * User role with optional outlet scoping. Maps to the {@code user_roles} table.
 * Role values: USER, DELIVERY_GUY, ADMIN.
 */
@Entity
@Table(name = "user_roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_role_outlet", columnNames = {"user_id", "role", "outlet_id"}))
@Getter
@Setter
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_userroles_user"))
    private User user;

    @Column(nullable = false, length = 30)
    private String role;

    @ManyToOne
    @JoinColumn(name = "outlet_id", foreignKey = @ForeignKey(name = "fk_userroles_outlet"))
    private Outlet outlet;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
