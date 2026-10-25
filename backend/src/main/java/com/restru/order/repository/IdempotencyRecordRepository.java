package com.restru.order.repository;

import com.restru.order.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access for {@link IdempotencyRecord}.
 */
@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {

    Optional<IdempotencyRecord> findByRecordKey(String recordKey);

    boolean existsByRecordKey(String recordKey);
}
