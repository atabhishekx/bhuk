package com.restru.menu.entity;

import com.restru.auth.entity.Outlet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Outlet-specific item availability / price override. Maps to the {@code outlet_item_availability} table.
 */
@Entity
@Table(name = "outlet_item_availability")
@Getter
@Setter
public class OutletItemAvailability {

    @EmbeddedId
    private OutletItemAvailabilityId id = new OutletItemAvailabilityId();

    @ManyToOne(optional = false)
    @MapsId("outletId")
    @JoinColumn(name = "outlet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_oia_outlet"))
    private Outlet outlet;

    @ManyToOne(optional = false)
    @MapsId("itemId")
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_oia_item"))
    private MenuItem item;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(name = "price_override_minor")
    private Integer priceOverrideMinor;
}
