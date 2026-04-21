package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.SettleResourcesCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.SettleResourcesUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.ResourceReservationRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Position;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Log4j2
@Service
@RequiredArgsConstructor
public class SettleResourcesService implements SettleResourcesUseCase {

    private final WalletRepository walletRepository;
    private final ResourceReservationRepository resourceReservationRepository;
    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public void execute(SettleResourcesCommand cmd) {
        var reservation = resourceReservationRepository.findByOrderIdWithLock(cmd.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada"));

        var wallet = walletRepository.findByIdWithLock(reservation.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada"));

        Position position = null;

        if (reservation.getType() == ReservationType.BALANCE) {
            int diff = cmd.filledQuantity() - reservation.getSettledVolume().intValue();
            if (diff > 0) {
                position = positionRepository.findByWalletIdAndTickerWithLock(wallet.getId(), cmd.ticker())
                        .orElseGet(() -> Position.create(wallet.getId(), cmd.ticker()));
                position.credit(diff);
            }
        } else {
            BigDecimal total = cmd.averagePrice().multiply(BigDecimal.valueOf(cmd.filledQuantity()));
            BigDecimal diff = total.subtract(reservation.getSettledVolume());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                wallet.credit(diff);
            }
        }

        reservation.updateVolume(cmd.filledQuantity(), cmd.averagePrice());

        if (isOrderFinished(cmd.status())) {
            if (reservation.getType() == ReservationType.BALANCE) {
                reservation.complete(wallet);
            } else {
                if (position == null) {
                    position = positionRepository.findByWalletIdAndTickerWithLock(wallet.getId(), cmd.ticker())
                            .orElseThrow(() -> new ResourceNotFoundException("Custódia não encontrada"));
                }
                reservation.complete(position);
            }
        }

        resourceReservationRepository.save(reservation);
        walletRepository.save(wallet);
        if (position != null) {
            positionRepository.save(position);
        }
    }

    private boolean isOrderFinished(OrderStatus status) {
        return status == OrderStatus.EXPIRED || status == OrderStatus.FILLED || status == OrderStatus.CANCELLED;
    }


}
