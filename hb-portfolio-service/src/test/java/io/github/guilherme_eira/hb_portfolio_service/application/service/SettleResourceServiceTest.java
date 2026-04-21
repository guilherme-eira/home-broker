package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.SettleResourcesCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.ResourceReservationRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationStatus;
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
class SettleResourceServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private ResourceReservationRepository resourceReservationRepository;

    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private SettleResourcesService settleResourceService;

    @Test
    void shouldSettleBalanceReservationSuccessfully() {
        var orderId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var ticker = "WEGE3";

        var cmd = new SettleResourcesCommand(
                orderId, ticker, OrderStatus.FILLED, 10, new BigDecimal("35.00")
        );

        var wallet = Wallet.create(mock(Investor.class));

        var reservation = ResourceReservation.forBalance(orderId, walletId, new BigDecimal("367.50"));
        var position = Position.create(wallet.getId(), ticker);

        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(reservation));
        given(walletRepository.findByIdWithLock(walletId)).willReturn(Optional.of(wallet));
        given(positionRepository.findByWalletIdAndTickerWithLock(any(), eq(ticker))).willReturn(Optional.of(position));

        settleResourceService.execute(cmd);

        assertEquals(10, position.getQuantity());
        assertEquals(0, new BigDecimal("17.50").compareTo(wallet.getAvailableBalance()));

        verify(resourceReservationRepository).save(reservation);
        verify(walletRepository).save(wallet);
        verify(positionRepository).save(position);

        ArgumentCaptor<ResourceReservation> reservationCaptor = ArgumentCaptor.forClass(ResourceReservation.class);
        verify(resourceReservationRepository).save(reservationCaptor.capture());
        ResourceReservation captured = reservationCaptor.getValue();

        assertEquals(0, new BigDecimal("350.00").compareTo(captured.getSettledVolume()));
        assertEquals(0, BigDecimal.ZERO.compareTo(captured.getRemainingVolume()));
        assertEquals(ReservationStatus.COMPLETED, captured.getStatus());
    }

    @Test
    void shouldCreatePositionAutomaticallyDuringBalanceSettlement() {
        var orderId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var ticker = "NOVO3";
        var cmd = new SettleResourcesCommand(orderId, ticker, OrderStatus.FILLED, 10, new BigDecimal("10.00"));

        var wallet = Wallet.create(mock(Investor.class));

        var reservation = ResourceReservation.forBalance(orderId, walletId, new BigDecimal("100.00"));

        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(reservation));
        given(walletRepository.findByIdWithLock(walletId)).willReturn(Optional.of(wallet));
        given(positionRepository.findByWalletIdAndTickerWithLock(any(), eq(ticker))).willReturn(Optional.empty());

        settleResourceService.execute(cmd);

        ArgumentCaptor<Position> positionCaptor = ArgumentCaptor.forClass(Position.class);
        verify(positionRepository).save(positionCaptor.capture());

        Position capturedPosition = positionCaptor.getValue();
        assertEquals(10, capturedPosition.getQuantity());
        assertEquals(ticker, capturedPosition.getTicker());
        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());

        verify(walletRepository).save(wallet);
        verify(resourceReservationRepository).save(reservation);
    }

    @Test
    void shouldSettleAssetReservationSuccessfully() {
        var orderId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var ticker = "VALE3";

        var cmd = new SettleResourcesCommand(
                orderId, ticker, OrderStatus.EXPIRED, 100, new BigDecimal("90.00")
        );

        var wallet = Wallet.create(mock(Investor.class));
        var reservation = ResourceReservation.forAsset(orderId, walletId, ticker, new BigDecimal("150"));
        var position = Position.create(wallet.getId(), ticker);

        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(reservation));
        given(walletRepository.findByIdWithLock(walletId)).willReturn(Optional.of(wallet));
        given(positionRepository.findByWalletIdAndTickerWithLock(any(), eq(ticker))).willReturn(Optional.of(position));

        settleResourceService.execute(cmd);

        assertEquals(0, new BigDecimal("9000.00").compareTo(wallet.getAvailableBalance()));
        assertEquals(50, position.getQuantity());

        verify(walletRepository).save(wallet);
        verify(positionRepository).save(position);

        ArgumentCaptor<ResourceReservation> reservationCaptor = ArgumentCaptor.forClass(ResourceReservation.class);
        verify(resourceReservationRepository).save(reservationCaptor.capture());

        ResourceReservation captured = reservationCaptor.getValue();
        assertEquals(0, new BigDecimal("100").compareTo(captured.getSettledVolume()));
        assertEquals(0, BigDecimal.ZERO.compareTo(captured.getRemainingVolume()));
        assertEquals(ReservationStatus.COMPLETED, captured.getStatus());
    }

    @Test
    void shouldSettlePartialBalanceSuccessfully() {
        var orderId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var ticker = "WEGE3";
        var cmd = new SettleResourcesCommand(
                orderId, ticker, OrderStatus.PARTIAL, 5, new BigDecimal("80.00")
        );

        var wallet = Wallet.create(mock(Investor.class));
        var reservation = ResourceReservation.forBalance(orderId, walletId, new BigDecimal("1000.00"));
        var position = Position.create(wallet.getId(), ticker);

        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(reservation));
        given(walletRepository.findByIdWithLock(walletId)).willReturn(Optional.of(wallet));
        given(positionRepository.findByWalletIdAndTickerWithLock(any(), eq(ticker))).willReturn(Optional.of(position));

        settleResourceService.execute(cmd);

        assertEquals(5, position.getQuantity());
        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getAvailableBalance()));

        verify(resourceReservationRepository).save(reservation);
        verify(walletRepository).save(wallet);
        verify(positionRepository).save(position);

        ArgumentCaptor<ResourceReservation> captor = ArgumentCaptor.forClass(ResourceReservation.class);
        verify(resourceReservationRepository).save(captor.capture());

        ResourceReservation captured = captor.getValue();
        assertEquals(0, new BigDecimal("400.00").compareTo(captured.getSettledVolume()));
        assertEquals(0, new BigDecimal("600.00").compareTo(captured.getRemainingVolume()));
        assertEquals(ReservationStatus.PENDING, captured.getStatus());
    }

    @Test
    void shouldSettlePartialAssetSuccessfully() {
        var orderId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var ticker = "VALE3";
        var cmd = new SettleResourcesCommand(
                orderId, ticker, OrderStatus.PARTIAL, 30, new BigDecimal("100.00")
        );

        var wallet = Wallet.create(mock(Investor.class));
        var reservation = ResourceReservation.forAsset(orderId, walletId, ticker, new BigDecimal("100"));

        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(reservation));
        given(walletRepository.findByIdWithLock(walletId)).willReturn(Optional.of(wallet));

        settleResourceService.execute(cmd);

        assertEquals(0, new BigDecimal("3000.00").compareTo(wallet.getAvailableBalance()));

        verify(walletRepository).save(wallet);
        verify(resourceReservationRepository).save(reservation);

        ArgumentCaptor<ResourceReservation> captor = ArgumentCaptor.forClass(ResourceReservation.class);
        verify(resourceReservationRepository).save(captor.capture());

        ResourceReservation captured = captor.getValue();
        assertEquals(0, new BigDecimal("30").compareTo(captured.getSettledVolume()));
        assertEquals(0, new BigDecimal("70").compareTo(captured.getRemainingVolume()));
        assertEquals(ReservationStatus.PENDING, captured.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenReservationNotFound() {
        var orderId = UUID.randomUUID();
        var cmd = new SettleResourcesCommand(
                orderId, "PETR4", OrderStatus.FILLED, 10, BigDecimal.TEN
        );

        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> settleResourceService.execute(cmd));

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(positionRepository);
        verify(resourceReservationRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        var orderId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var cmd = new SettleResourcesCommand(orderId,"PETR4", OrderStatus.FILLED, 10, BigDecimal.TEN);

        var reservation = mock(ResourceReservation.class);
        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(reservation));
        given(reservation.getWalletId()).willReturn(walletId);
        given(walletRepository.findByIdWithLock(walletId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> settleResourceService.execute(cmd));

        verifyNoInteractions(positionRepository);
        verify(resourceReservationRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPositionNotFoundDuringAssetSettlement() {
        var orderId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var ticker = "VALE3";
        var cmd = new SettleResourcesCommand(orderId, ticker, OrderStatus.FILLED, 100, new BigDecimal("90.00"));

        var wallet = Wallet.create(mock(Investor.class));
        var reservation = ResourceReservation.forAsset(orderId, walletId, ticker, new BigDecimal("100"));

        given(resourceReservationRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(reservation));
        given(walletRepository.findByIdWithLock(walletId)).willReturn(Optional.of(wallet));
        given(positionRepository.findByWalletIdAndTickerWithLock(any(), eq(ticker))).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> settleResourceService.execute(cmd));

        verify(resourceReservationRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        verify(positionRepository, never()).save(any());
    }
}