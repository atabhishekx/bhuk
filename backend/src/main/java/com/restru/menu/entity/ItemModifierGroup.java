package com.restru.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Link between a menu item and a modifier group. Maps to the {@code item_modifier_groups} table.
 */
@Entity
@Table(name = "item_modifier_groups")
@Getter
@Setter
public class ItemModifierGroup {

    @EmbeddedId
    private ItemModifierGroupId id = new ItemModifierGroupId();

    @ManyToOne(optional = false)
    @MapsId("itemId")
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_img_item"))
    private MenuItem item;

    @ManyToOne(optional = false)
    @MapsId("modifierGroupId")
    @JoinColumn(name = "modifier_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_img_modifier_group"))
    private ModifierGroup modifierGroup;
}
