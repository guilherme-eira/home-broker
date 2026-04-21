package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByOwnerId(UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.id = :id")
    Optional<WalletEntity> findByIdWithLock(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.owner.id = :ownerId")
    Optional<WalletEntity> findByOwnerIdWithLock(UUID ownerId);
}
