package com.restru.delivery.repository;

import com.restru.delivery.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link DeliveryPartner}.
 */
@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long>, JpaSpecificationExecutor<DeliveryPartner> {

    DeliveryPartner findByUserId(Long userId);

    List<DeliveryPartner> findByOutletIdAndStatus(Long outletId, String status);

    List<DeliveryPartner> findByStatus(String status);

    List<DeliveryPartner> findByStatusOrderByUpdatedAtDesc(String status);
}
