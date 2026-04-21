package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.ResourceReservationEntity;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.projection.BlockedAssetProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceReservationJpaRepository extends JpaRepository<ResourceReservationEntity, UUID> {

    @Query("SELECT COALESCE(SUM(r.remainingVolume), 0) " +
            "FROM ResourceReservationEntity r " +
            "WHERE r.walletId = :walletId " +
            "AND r.type = 'BALANCE' " +
            "AND r.status = 'PENDING'")
    BigDecimal getBlockedBalance(UUID walletId);

    @Query("SELECT r.ticker as ticker, SUM(r.remainingVolume) as quantity " +
            "FROM ResourceReservationEntity r " +
            "WHERE r.walletId = :walletId " +
            "AND r.type = 'ASSET' " +
            "AND r.status = 'PENDING' " +
            "GROUP BY r.ticker")
    List<BlockedAssetProjection> findAllBlockedAssets(UUID walletId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ResourceReservationEntity r WHERE r.orderId = :orderId")
    Optional<ResourceReservationEntity> findByOrderIdWithLock(UUID orderId);
}
