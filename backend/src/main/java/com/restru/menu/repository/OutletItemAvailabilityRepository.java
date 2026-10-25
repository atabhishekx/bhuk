package com.restru.menu.repository;

import com.restru.menu.entity.OutletItemAvailability;
import com.restru.menu.entity.OutletItemAvailabilityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link OutletItemAvailability}.
 */
@Repository
public interface OutletItemAvailabilityRepository extends JpaRepository<OutletItemAvailability, OutletItemAvailabilityId> {

    List<OutletItemAvailability> findByOutletId(Long outletId);
}
