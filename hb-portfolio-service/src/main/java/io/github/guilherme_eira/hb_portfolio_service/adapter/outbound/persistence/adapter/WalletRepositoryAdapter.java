package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper.WalletMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository.WalletJpaRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepository {

    private final WalletJpaRepository repository;
    private final WalletMapper mapper;

    @Override
    public Wallet save(Wallet wallet) {
        return mapper.toDomain(repository.save(mapper.toEntity(wallet)));
    }

    @Override
    public Optional<Wallet> findByOwnerId(UUID ownerId) {
        return repository.findByOwnerId(ownerId).map(mapper::toDomain);
    }

    @Override
    public Optional<Wallet> findByIdWithLock(UUID id) {
        return repository.findByIdWithLock(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Wallet> findByOwnerIdWithLock(UUID ownerId) {
        return repository.findByOwnerIdWithLock(ownerId).map(mapper::toDomain);
    }
}
