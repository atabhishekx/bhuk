package com.restru.menu.repository;

import com.restru.menu.entity.ModifierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link ModifierGroup}.
 */
@Repository
public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, Long> {

    List<ModifierGroup> findByOutletIdAndActiveTrue(Long outletId);
}
