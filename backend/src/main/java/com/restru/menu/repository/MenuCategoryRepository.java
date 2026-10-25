package com.restru.menu.repository;

import com.restru.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@link MenuCategory}.
 */
@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
}
