package io.github.guilherme_eira.hb_portfolio_service.domain.model;

import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WalletTest {

    @Test
    void shouldCreateWalletWithZeroBalance() {
        var investor = mock(Investor.class);
        var wallet = Wallet.create(investor);

        assertNotNull(wallet.getId());
        assertEquals(BigDecimal.ZERO, wallet.getAvailableBalance());
        assertEquals(investor, wallet.getOwner());
    }

    @Test
    void shouldCreditAmount() {
        var wallet = Wallet.create(mock(Investor.class));

        wallet.credit(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), wallet.getAvailableBalance());
    }

    @Test
    void shouldThrowExceptionWhenCreditIsInvalid() {
        var wallet = Wallet.create(mock(Investor.class));

        assertThrows(BusinessException.class, () -> wallet.credit(new BigDecimal("-10.00")));
        assertThrows(BusinessException.class, () -> wallet.credit(BigDecimal.ZERO));
    }

    @Test
    void shouldReserveBalance() {
        var wallet = Wallet.create(mock(Investor.class));
        wallet.credit(new BigDecimal("100.00"));

        wallet.reserve(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), wallet.getAvailableBalance());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficientForReservation() {
        var wallet = Wallet.create(mock(Investor.class));
        wallet.credit(new BigDecimal("50.00"));

        var exception = assertThrows(BusinessException.class, () ->
                wallet.reserve(new BigDecimal("50.01"))
        );

        assertEquals("Saldo insuficiente para reserva.", exception.getMessage());
    }
}