package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetJpaRepository extends JpaRepository<AssetEntity, String> {
    Optional<AssetEntity> findByTicker(String ticker);
}
