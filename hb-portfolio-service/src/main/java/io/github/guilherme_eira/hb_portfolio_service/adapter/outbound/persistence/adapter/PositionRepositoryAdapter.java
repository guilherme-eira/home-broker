package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper.PositionMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository.PositionJpaRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PositionRepositoryAdapter implements PositionRepository {

    private final PositionJpaRepository repository;
    private final PositionMapper mapper;

    @Override
    public Position save(Position position) {
        return mapper.toDomain(repository.save(mapper.toEntity(position)));
    }

    @Override
    public Optional<Position> findByWalletIdAndTickerWithLock(UUID walletId, String ticker) {
        return repository.findByWalletIdAndTickerWithLock(walletId, ticker).map(mapper::toDomain);
    }

    @Override
    public Page<Position> findByWalletId(UUID walletId, Pageable pageable) {
        return repository.findByWalletId(walletId, pageable).map(mapper::toDomain);
    }
}
