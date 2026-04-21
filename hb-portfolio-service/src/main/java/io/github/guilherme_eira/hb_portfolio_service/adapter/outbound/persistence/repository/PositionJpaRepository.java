package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.PositionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PositionJpaRepository extends JpaRepository<PositionEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PositionEntity p WHERE p.walletId = :walletId AND p.ticker = :ticker")
    Optional<PositionEntity> findByWalletIdAndTickerWithLock(UUID walletId, String ticker);
    Page<PositionEntity> findByWalletId(UUID walletId, Pageable pageable);

}
