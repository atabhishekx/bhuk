package com.restru.delivery.repository;

import com.restru.delivery.entity.DeliveryIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link DeliveryIssue}.
 */
@Repository
public interface DeliveryIssueRepository extends JpaRepository<DeliveryIssue, Long> {

    List<DeliveryIssue> findByAssignmentId(Long assignmentId);
}
