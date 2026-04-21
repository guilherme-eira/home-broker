package io.github.guilherme_eira.hb_portfolio_service.domain.model;

import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ResourceReservationTest {

    @Test
    void shouldSettleBalanceReservationAndReturnRemaining() {
        var wallet = Wallet.create(mock(Investor.class));
        wallet.credit(new BigDecimal("1000.00"));
        wallet.reserve(new BigDecimal("1000.00"));

        var reservation = ResourceReservation.forBalance(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000.00"));

        reservation.updateVolume(10, new BigDecimal("80.00"));
        reservation.complete(wallet);

        assertEquals(new BigDecimal("200.00"), wallet.getAvailableBalance(), "A wallet deve receber o estorno do saldo não utilizado");
        assertEquals(new BigDecimal("800.00"), reservation.getSettledVolume());
    }

    @Test
    void shouldSettleAssetReservationAndReturnRemainingToPosition() {
        var wallet = mock(Wallet.class);
        var position = Position.create(UUID.randomUUID(), "PETR4");
        position.credit(100);
        position.reserve(100);

        var reservation = ResourceReservation.forAsset(UUID.randomUUID(), UUID.randomUUID(), "PETR4", new BigDecimal("100"));

        reservation.updateVolume(70, new BigDecimal("30.00"));
        reservation.complete(position);

        assertEquals(30, position.getQuantity(), "As ações não vendidas devem retornar para a Position");
        assertEquals(new BigDecimal("70"), reservation.getSettledVolume());
    }

    @Test
    void shouldNotAllowUpdateAfterCompletion() {
        var reservation = ResourceReservation.forBalance(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN);
        reservation.complete(mock(Wallet.class));

        assertThrows(BusinessException.class, () ->
                reservation.updateVolume(5, BigDecimal.ONE));
    }
}