package io.github.guilherme_eira.hb_portfolio_service.domain.model;

import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PositionTest {

    @Test
    void shouldCreatePositionWithZeroQuantity() {
        var walletId = UUID.randomUUID();
        var ticker = "ITUB4";

        var position = Position.create(walletId, ticker);

        assertNotNull(position.getId());
        assertEquals(ticker, position.getTicker());
        assertEquals(0, position.getQuantity());
        assertEquals(walletId, position.getWalletId());
    }

    @Test
    void shouldCreditAssets() {
        var position = Position.create(UUID.randomUUID(), "VALE3");

        position.credit(50);

        assertEquals(50, position.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenCreditIsInvalid() {
        var position = Position.create(UUID.randomUUID(), "VALE3");

        var exception = assertThrows(BusinessException.class, () -> position.credit(0));
        assertEquals("Quantidade inválida.", exception.getMessage());

        assertThrows(BusinessException.class, () -> position.credit(-10));
    }

    @Test
    void shouldReserveAssets() {
        var position = Position.create(UUID.randomUUID(), "PETR4");
        position.credit(100);

        position.reserve(40);

        assertEquals(60, position.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenAssetsAreInsufficient() {
        var position = Position.create(UUID.randomUUID(), "PETR4");
        position.credit(10);

        var exception = assertThrows(BusinessException.class, () ->
                position.reserve(11)
        );

        assertEquals("Quantidade de ações insuficiente para venda.", exception.getMessage());
    }
}