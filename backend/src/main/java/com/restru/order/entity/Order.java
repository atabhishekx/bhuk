package com.restru.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Order — the authoritative order record. Order state and payment state are strictly separate.
 * Maps to the {@code orders} table.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String orderNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_customer"))
    private com.restru.auth.entity.User customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "outlet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_outlet"))
    private com.restru.auth.entity.Outlet outlet;

    @Column(nullable = false, length = 30)
    private String status = "PLACED";

    @Column(nullable = false, name = "subtotal_minor")
    private Integer subtotalMinor = 0;

    @Column(nullable = false, name = "tax_minor")
    private Integer taxMinor = 0;

    @Column(nullable = false, name = "delivery_fee_minor")
    private Integer deliveryFeeMinor = 0;

    @Column(nullable = false, name = "grand_total_minor")
    private Integer grandTotalMinor = 0;

    @Column(nullable = false, name = "delivery_address_json", columnDefinition = "JSON")
    private String deliveryAddressJson;

    @Column(name = "idempotency_key", length = 128, unique = true)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
