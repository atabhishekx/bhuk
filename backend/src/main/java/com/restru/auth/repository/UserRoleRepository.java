package com.restru.auth.repository;

import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Data access for {@link UserRole}.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.user = ?1")
    void deleteByUser(User user);
}
