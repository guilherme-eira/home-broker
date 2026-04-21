package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity.AssetRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface AssetRuleJpaRepository extends JpaRepository<AssetRuleEntity, String> {
    Optional<AssetRuleEntity> findByTicker(String ticker);
    @Query("SELECT a.referencePrice FROM AssetRuleEntity a WHERE a.ticker = :ticker")
    BigDecimal findReferencePriceByTicker(String ticker);
}
