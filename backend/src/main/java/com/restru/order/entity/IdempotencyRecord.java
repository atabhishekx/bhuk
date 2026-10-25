package com.restru.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Idempotency record to prevent duplicate processing of order/payment/assignment operations.
 * Maps to the {@code idempotency_records} table.
 */
@Entity
@Table(name = "idempotency_records")
@Getter
@Setter
public class IdempotencyRecord {

    @Id
    @Column(nullable = false, length = 128, name = "record_key")
    private String recordKey;

    @ManyToOne
    @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_idempotency_actor"))
    private com.restru.auth.entity.User actor;

    @Column(nullable = false, length = 60)
    private String operation;

    @Column(nullable = false, name = "request_hash", length = 128)
    private String requestHash;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "JSON")
    private String responseBody;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;
}
