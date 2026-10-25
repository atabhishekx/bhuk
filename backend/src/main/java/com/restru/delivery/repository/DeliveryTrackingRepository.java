package com.restru.delivery.repository;

import com.restru.delivery.entity.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link DeliveryTracking}.
 */
@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {

    List<DeliveryTracking> findByAssignmentIdOrderByRecordedAtDesc(Long assignmentId);

    List<DeliveryTracking> findTop5ByAssignmentIdOrderByRecordedAtDesc(Long assignmentId);
}
