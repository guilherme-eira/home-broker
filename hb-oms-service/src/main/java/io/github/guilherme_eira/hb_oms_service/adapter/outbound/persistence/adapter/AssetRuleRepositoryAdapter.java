package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.mapper.AssetRuleMapper;
import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.repository.AssetRuleJpaRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.AssetRuleRepository;
import io.github.guilherme_eira.hb_oms_service.domain.vo.AssetRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssetRuleRepositoryAdapter implements AssetRuleRepository {
    private final AssetRuleJpaRepository repository;
    private final AssetRuleMapper mapper;

    @Override
    public List<AssetRule> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
