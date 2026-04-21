package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.ResourcesOutput;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.GetResourcesUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.ResourceReservationRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetResourcesService implements GetResourcesUseCase {

    private final WalletRepository walletRepository;
    private final PositionRepository positionRepository;
    private final ResourceReservationRepository resourceReservationRepository;

    @Override
    public ResourcesOutput execute(UUID userId, Pageable pageable) {
        var wallet = walletRepository.findByOwnerId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a carteira associada a este id de usuário"));

        var totalBlocked = resourceReservationRepository.getBlockedBalance(wallet.getId());

        var positions = positionRepository.findByWalletId(wallet.getId(), pageable);

        var blockedAssets = resourceReservationRepository.findAllBlockedAssets(wallet.getId());

        return new ResourcesOutput(
                wallet.getAvailableBalance(),
                totalBlocked,
                positions.map(p -> {
                    return new ResourcesOutput.PositionOutputDTO(
                            p.getTicker(),
                            p.getQuantity(),
                            blockedAssets.getOrDefault(p.getTicker(), 0)
                    );
                })
        );
    }
}
