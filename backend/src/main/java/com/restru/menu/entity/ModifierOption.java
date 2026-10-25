package com.restru.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Modifier option (e.g. Mild, Extra Cheese). Maps to the {@code modifier_options} table.
 */
@Entity
@Table(name = "modifier_options")
@Getter
@Setter
public class ModifierOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_modifier_options_group"))
    private ModifierGroup group;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, name = "price_delta_minor")
    private Integer priceDeltaMinor = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
