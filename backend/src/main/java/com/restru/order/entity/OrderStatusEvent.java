package com.restru.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Append-only audit of order status transitions.
 * Maps to the {@code order_status_events} table.
 */
@Entity
@Table(name = "order_status_events")
@Getter
@Setter
public class OrderStatusEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_status_events_order"))
    private Order order;

    @Column(name = "old_status", length = 30)
    private String oldStatus;

    @Column(nullable = false, name = "new_status", length = 30)
    private String newStatus;

    @ManyToOne
    @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_order_status_events_actor"))
    private com.restru.auth.entity.User actor;

    @Column(name = "actor_role", length = 30)
    private String actorRole;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
