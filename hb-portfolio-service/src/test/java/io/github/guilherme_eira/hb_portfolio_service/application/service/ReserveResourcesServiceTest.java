package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.ReserveResourcesCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.ResourceReservationRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;
import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Investor;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Position;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.ResourceReservation;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReserveResourcesServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private ResourceReservationRepository resourceReservationRepository;

    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private ReserveResourcesService reserveResourcesService;

    @Test
    void shouldReserveBalanceSuccessfully() {
        var investorId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var amount = new BigDecimal("1000.00");
        var cmd = new ReserveResourcesCommand(investorId, orderId, OrderSide.BID, null, amount);

        var wallet = Wallet.create(mock(Investor.class));
        wallet.credit(new BigDecimal("2000.00"));

        given(walletRepository.findByOwnerIdWithLock(investorId)).willReturn(Optional.of(wallet));

        reserveResourcesService.execute(cmd);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(new BigDecimal("1000.00"), walletCaptor.getValue().getAvailableBalance());

        ArgumentCaptor<ResourceReservation> reservationCaptor = ArgumentCaptor.forClass(ResourceReservation.class);
        verify(resourceReservationRepository).save(reservationCaptor.capture());

        ResourceReservation captured = reservationCaptor.getValue();
        assertEquals(orderId, captured.getOrderId());
        assertEquals(amount, captured.getTotalVolume());
    }

    @Test
    void shouldReserveAssetsSuccessfully() {
        var investorId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var ticker = "PETR4";
        var quantity = new BigDecimal("50");
        var cmd = new ReserveResourcesCommand(investorId, orderId, OrderSide.ASK, ticker, quantity);

        var wallet = mock(Wallet.class);
        given(wallet.getId()).willReturn(walletId);

        var position = Position.create(walletId, ticker);
        position.credit(100);

        given(walletRepository.findByOwnerIdWithLock(investorId)).willReturn(Optional.of(wallet));
        given(positionRepository.findByWalletIdAndTickerWithLock(walletId, ticker)).willReturn(Optional.of(position));

        reserveResourcesService.execute(cmd);

        ArgumentCaptor<ResourceReservation> reservationCaptor = ArgumentCaptor.forClass(ResourceReservation.class);
        verify(resourceReservationRepository).save(reservationCaptor.capture());

        assertEquals(ticker, reservationCaptor.getValue().getTicker());
        assertEquals(50, position.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        var cmd = new ReserveResourcesCommand(UUID.randomUUID(), UUID.randomUUID(), OrderSide.BID, null, BigDecimal.TEN);
        given(walletRepository.findByOwnerIdWithLock(any())).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reserveResourcesService.execute(cmd));
        verifyNoInteractions(resourceReservationRepository);
    }

    @Test
    void shouldThrowExceptionWhenPositionNotFound() {
        var investorId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var cmd = new ReserveResourcesCommand(investorId, UUID.randomUUID(), OrderSide.ASK, "VALE3", BigDecimal.TEN);

        var wallet = mock(Wallet.class);
        given(wallet.getId()).willReturn(walletId);
        given(walletRepository.findByOwnerIdWithLock(investorId)).willReturn(Optional.of(wallet));
        given(positionRepository.findByWalletIdAndTickerWithLock(walletId, "VALE3")).willReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> reserveResourcesService.execute(cmd));
    }
}