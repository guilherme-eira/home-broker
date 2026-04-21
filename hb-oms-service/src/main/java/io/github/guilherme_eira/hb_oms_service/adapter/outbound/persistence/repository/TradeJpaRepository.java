package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeJpaRepository extends JpaRepository<TradeEntity, UUID> {
    boolean existsById(UUID id);
}
