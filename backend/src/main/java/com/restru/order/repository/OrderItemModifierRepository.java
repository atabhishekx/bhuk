package com.restru.order.repository;

import com.restru.order.entity.OrderItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link OrderItemModifier}.
 */
@Repository
public interface OrderItemModifierRepository extends JpaRepository<OrderItemModifier, Long> {

    List<OrderItemModifier> findByOrderItemId(Long orderItemId);
}
