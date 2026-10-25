package com.restru.menu.entity;

import com.restru.auth.entity.Outlet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Modifier group (e.g. Spice Level, Add-ons). Maps to the {@code modifier_groups} table.
 */
@Entity
@Table(name = "modifier_groups")
@Getter
@Setter
public class ModifierGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "outlet_id", foreignKey = @ForeignKey(name = "fk_modifier_groups_outlet"))
    private Outlet outlet;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, name = "min_select")
    private Integer minSelect = 0;

    @Column(nullable = false, name = "max_select")
    private Integer maxSelect = 0;

    @Column(nullable = false)
    private Boolean required = false;

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
