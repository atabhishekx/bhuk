package com.restru.auth.repository;

import com.restru.auth.entity.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link Outlet}.
 */
@Repository
public interface OutletRepository extends JpaRepository<Outlet, Long> {

    List<Outlet> findByStatus(String status);
}
