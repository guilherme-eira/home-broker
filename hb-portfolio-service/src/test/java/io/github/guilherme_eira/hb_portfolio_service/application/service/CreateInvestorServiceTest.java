package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.CreateInvestorCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.InvestorRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Investor;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateInvestorServiceTest {

    @Mock
    private InvestorRepository investorRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private CreateInvestorService createInvestorService;

    @Test
    void shouldCreateInvestorAndWalletSuccessfully() {
        var userIdStr = UUID.randomUUID().toString();
        var userId = UUID.fromString(userIdStr);

        var cmd = new CreateInvestorCommand(
                userIdStr,
                "Guilherme Eira",
                "gui@email.com",
                "12345678909",
                "guieira",
                Instant.now()
        );

        var realInvestor = Investor.create(
                userId,
                cmd.fullName(),
                cmd.email(),
                cmd.taxId(),
                cmd.username(),
                cmd.createdAt()
        );

        given(investorRepository.existsById(userId)).willReturn(false);
        given(investorRepository.save(any(Investor.class))).willReturn(realInvestor);

        createInvestorService.execute(cmd);

        InOrder inOrder = inOrder(investorRepository, walletRepository);

        ArgumentCaptor<Investor> investorCaptor = ArgumentCaptor.forClass(Investor.class);
        inOrder.verify(investorRepository).save(investorCaptor.capture());

        Investor capturedInvestor = investorCaptor.getValue();
        assertEquals(userId, capturedInvestor.getId());
        assertEquals("Guilherme Eira", capturedInvestor.getFullName());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        inOrder.verify(walletRepository).save(walletCaptor.capture());

        Wallet capturedWallet = walletCaptor.getValue();
        assertEquals(userId, capturedWallet.getOwner().getId());
    }

    @Test
    void shouldReturnImmediatelyIfInvestorAlreadyExists() {
        var userId = UUID.randomUUID();
        var cmd = new CreateInvestorCommand(userId.toString(), "Guilherme", "gui@email.com", "123", "gui", Instant.now());

        given(investorRepository.existsById(userId)).willReturn(true);

        createInvestorService.execute(cmd);

        verify(investorRepository).existsById(userId);

        verify(investorRepository, never()).save(any());
        verifyNoInteractions(walletRepository);
    }

}