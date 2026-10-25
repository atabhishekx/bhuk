package com.restru.menu.repository;

import com.restru.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link MenuItem}.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategoryIdAndActiveTrueAndAvailableTrue(Long categoryId);

    List<MenuItem> findByActiveTrueAndAvailableTrue();
}
