package com.restru.cart.repository;

import com.restru.cart.entity.CartItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link CartItemModifier}.
 */
@Repository
public interface CartItemModifierRepository extends JpaRepository<CartItemModifier, Long> {

    List<CartItemModifier> findByCartItemId(Long cartItemId);
}
