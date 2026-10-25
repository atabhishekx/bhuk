package com.restru.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Selected modifier option on a cart line item. Maps to the {@code cart_item_modifiers} table.
 */
@Entity
@Table(name = "cart_item_modifiers")
@Getter
@Setter
public class CartItemModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cim_cart_item"))
    private CartItem cartItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "modifier_option_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cim_modifier_option"))
    private com.restru.menu.entity.ModifierOption modifierOption;

    @Column(nullable = false)
    private Integer quantity = 1;
}
