package com.restru.delivery.repository;

import com.restru.delivery.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link DeliveryAssignment}.
 */
@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long>, JpaSpecificationExecutor<DeliveryAssignment> {

    Optional<DeliveryAssignment> findByOrderId(Long orderId);

    List<DeliveryAssignment> findByPartnerId(Long partnerId);

    List<DeliveryAssignment> findByPartnerIdAndStateIn(Long partnerId, List<String> states);

    List<DeliveryAssignment> findByState(String state);

    List<DeliveryAssignment> findByStateIn(List<String> states);

    Optional<DeliveryAssignment> findByOrderIdAndStateNotIn(Long orderId, List<String> terminalStates);

    @Query("SELECT da FROM DeliveryAssignment da WHERE da.state = 'OFFERED' AND da.order.outlet.id = ?1 ORDER BY da.createdAt DESC")
    List<DeliveryAssignment> findOfferedByOutletId(Long outletId);
}
