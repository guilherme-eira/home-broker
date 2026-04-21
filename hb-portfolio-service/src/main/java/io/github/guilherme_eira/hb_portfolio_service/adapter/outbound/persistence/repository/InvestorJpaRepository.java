package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.InvestorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvestorJpaRepository extends JpaRepository<InvestorEntity, UUID> {
    boolean existsById(UUID id);
}
