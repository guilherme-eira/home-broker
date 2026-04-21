package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper.AssetMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository.AssetJpaRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.AssetRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.vo.Asset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AssetRepositoryAdapter implements AssetRepository {

    private final AssetJpaRepository repository;
    private final AssetMapper mapper;

    @Override
    public Optional<Asset> findByTicker(String ticker) {
        return repository.findByTicker(ticker).map(mapper::toDomain);
    }
}
