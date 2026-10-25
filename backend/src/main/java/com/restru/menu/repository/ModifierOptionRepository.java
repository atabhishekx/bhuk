package com.restru.menu.repository;

import com.restru.menu.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link ModifierOption}.
 */
@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, Long> {

    List<ModifierOption> findByGroupIdAndActiveTrue(Long groupId);
}
