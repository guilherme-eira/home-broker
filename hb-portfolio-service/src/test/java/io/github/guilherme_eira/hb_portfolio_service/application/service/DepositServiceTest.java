package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.DepositCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Investor;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private DepositService depositService;

    @Test
    void shouldDepositSuccessfully() {
        var investorId = UUID.randomUUID();
        var initialBalance = new BigDecimal("100.00");
        var depositAmount = new BigDecimal("50.00");
        var expectedBalance = new BigDecimal("150.00");

        var cmd = new DepositCommand(investorId, depositAmount);

        var investor = mock(Investor.class);
        var wallet = Wallet.create(investor);
        wallet.credit(initialBalance);

        given(walletRepository.findByOwnerIdWithLock(investorId)).willReturn(Optional.of(wallet));

        depositService.execute(cmd);

        verify(walletRepository).findByOwnerIdWithLock(investorId);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();
        assertEquals(expectedBalance, savedWallet.getAvailableBalance());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        var investorId = UUID.randomUUID();
        var cmd = new DepositCommand(investorId, new BigDecimal("100.00"));

        given(walletRepository.findByOwnerIdWithLock(investorId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> depositService.execute(cmd));

        verify(walletRepository, never()).save(any());
    }
}