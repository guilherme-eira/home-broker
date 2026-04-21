package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.ReserveResourcesCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.ReserveResourcesUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.ResourceReservationRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.ResourceReservation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReserveResourcesService implements ReserveResourcesUseCase {

    private final WalletRepository walletRepository;
    private final ResourceReservationRepository resourceReservationRepository;
    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public void execute(ReserveResourcesCommand cmd) {
        var wallet = walletRepository.findByOwnerIdWithLock(cmd.investorId())
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a carteira associada a este id de usuário"));

        if (cmd.side() == OrderSide.BID) {
            wallet.reserve(cmd.volume());
            walletRepository.save(wallet);
            var newReservation = ResourceReservation.forBalance(cmd.orderId(), wallet.getId(), cmd.volume());
            resourceReservationRepository.save(newReservation);
        } else {
            var position = positionRepository.findByWalletIdAndTickerWithLock(wallet.getId(), cmd.ticker())
                    .orElseThrow(() -> new BusinessException("Não há ações disponíveis."));
            position.reserve(cmd.volume().intValue());
            positionRepository.save(position);
            var newReservation = ResourceReservation.forAsset(cmd.orderId(), wallet.getId(), cmd.ticker(), cmd.volume());
            resourceReservationRepository.save(newReservation);
        }
    }
}
