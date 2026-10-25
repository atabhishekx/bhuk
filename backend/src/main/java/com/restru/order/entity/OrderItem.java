package com.restru.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Order line item with snapshot of name and price at purchase time.
 * Maps to the {@code order_items} table.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_items_order"))
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id", foreignKey = @ForeignKey(name = "fk_order_items_item"))
    private com.restru.menu.entity.MenuItem item;

    @Column(nullable = false, name = "item_name_snapshot", length = 200)
    private String itemNameSnapshot;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, name = "unit_price_minor")
    private Integer unitPriceMinor;

    @Column(nullable = false, name = "line_total_minor")
    private Integer lineTotalMinor;

    @Column(length = 255)
    private String instructions;

    @OneToMany(mappedBy = "orderItem", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<OrderItemModifier> modifiers = new java.util.LinkedHashSet<>();
}
