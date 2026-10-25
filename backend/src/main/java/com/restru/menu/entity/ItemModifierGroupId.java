package com.restru.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite key for {@link ItemModifierGroup}.
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class ItemModifierGroupId implements Serializable {

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "modifier_group_id")
    private Long modifierGroupId;
}
