package com.restru.order.repository;

import com.restru.order.entity.OrderStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link OrderStatusEvent}.
 */
@Repository
public interface OrderStatusEventRepository extends JpaRepository<OrderStatusEvent, Long> {

    List<OrderStatusEvent> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
