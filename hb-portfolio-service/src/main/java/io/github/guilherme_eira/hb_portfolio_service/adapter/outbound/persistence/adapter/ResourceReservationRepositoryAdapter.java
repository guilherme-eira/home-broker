package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper.ResourceReservationMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper.WalletMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.projection.BlockedAssetProjection;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository.ResourceReservationJpaRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.ResourceReservationRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.ResourceReservation;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ResourceReservationRepositoryAdapter implements ResourceReservationRepository {

    private final ResourceReservationJpaRepository repository;
    private final ResourceReservationMapper resourceReservationMapper;

    @Override
    public void save(ResourceReservation resourceReservation) {
        repository.save(resourceReservationMapper.toEntity(resourceReservation));
    }

    @Override
    public BigDecimal getBlockedBalance(UUID walletId) {
        return repository.getBlockedBalance(walletId);
    }

    @Override
    public Map<String, Integer> findAllBlockedAssets(UUID walletId) {
        var assets = repository.findAllBlockedAssets(walletId);
        return assets.stream()
                .collect(Collectors.toMap(
                        BlockedAssetProjection::ticker,
                        a -> a.quantity().intValue()
                ));
    }

    @Override
    public Optional<ResourceReservation> findByOrderIdWithLock(UUID orderId) {
        return repository.findByOrderIdWithLock(orderId).map(resourceReservationMapper::toDomain);
    }
}
