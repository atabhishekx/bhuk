package com.restru.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite key for {@link OutletItemAvailability}.
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class OutletItemAvailabilityId implements Serializable {

    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "item_id")
    private Long itemId;
}
