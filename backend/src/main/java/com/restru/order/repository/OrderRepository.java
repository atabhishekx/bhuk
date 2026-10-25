package com.restru.order.repository;

import com.restru.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link Order}.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findByOutletIdAndStatusOrderByCreatedAtDesc(Long outletId, String status);

    List<Order> findByOutletIdOrderByCreatedAtDesc(Long outletId);

    @Query("SELECT MAX(o.orderNumber) FROM Order o WHERE o.orderNumber LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxOrderNumberByPrefix(String prefix);
}
