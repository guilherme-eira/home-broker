package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity.OrderEntity;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderEntity o WHERE id = :id")
    Optional<OrderEntity> findByIdWithLock(UUID id);
    @Query("SELECT o FROM OrderEntity o WHERE o.investorId = :investorId AND (:status IS NULL OR o.status = :status)")
    Page<OrderEntity> findByInvestorIdAndOrderStatus(UUID investorId, OrderStatus status, Pageable pageable);
}
