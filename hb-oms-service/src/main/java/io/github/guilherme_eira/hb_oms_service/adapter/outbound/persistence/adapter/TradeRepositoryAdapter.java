package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.mapper.TradeMapper;
import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.repository.TradeJpaRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.TradeRepository;
import io.github.guilherme_eira.hb_oms_service.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TradeRepositoryAdapter implements TradeRepository {

    private final TradeJpaRepository repository;
    private final TradeMapper mapper;

    @Override
    public void save(Trade trade) {
        repository.save(mapper.toEntity(trade));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
