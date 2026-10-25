package com.restru.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Snapshot of a selected modifier on an order line item.
 * Maps to the {@code order_item_modifiers} table.
 */
@Entity
@Table(name = "order_item_modifiers")
@Getter
@Setter
public class OrderItemModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_modifiers_order_item"))
    private OrderItem orderItem;

    @Column(nullable = false, name = "name_snapshot", length = 100)
    private String nameSnapshot;

    @Column(nullable = false, name = "price_delta_minor")
    private Integer priceDeltaMinor = 0;

    @Column(nullable = false)
    private Integer quantity = 1;
}
