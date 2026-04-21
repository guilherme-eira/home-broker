package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.AddPositionCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.AssetRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Position;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import io.github.guilherme_eira.hb_portfolio_service.domain.vo.Asset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddPositionServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private AssetRepository assetRepository;

    @InjectMocks
    private AddPositionService service;

    @Test
    void shouldAddQuantityToExistingPosition() {
        var investorId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var cmd = new AddPositionCommand(investorId, "WEGE3", 100);

        given(assetRepository.findByTicker("WEGE3")).willReturn(Optional.of(new Asset("WEGE3")));

        var wallet = mock(Wallet.class);
        given(wallet.getId()).willReturn(walletId);
        given(walletRepository.findByOwnerId(investorId)).willReturn(Optional.of(wallet));

        var position = mock(Position.class);
        given(positionRepository.findByWalletIdAndTickerWithLock(walletId, "WEGE3"))
                .willReturn(Optional.of(position));

        service.execute(cmd);

        verify(position).credit(100);
        verify(positionRepository).save(position);
    }

    @Test
    void shouldThrowExceptionWhenAssetDoesNotExist() {
        var cmd = new AddPositionCommand(UUID.randomUUID(), "INVALID3", 100);
        given(assetRepository.findByTicker("INVALID3")).willReturn(Optional.empty());

        var exception = assertThrows(BusinessException.class, () -> service.execute(cmd));
        assertEquals("Ativo não encontrado", exception.getMessage());

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(positionRepository);
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        var cmd = new AddPositionCommand(UUID.randomUUID(), "PETR4", 50);

        given(assetRepository.findByTicker("PETR4")).willReturn(Optional.of(new Asset("PETR4")));
        given(walletRepository.findByOwnerId(any())).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(cmd));
        verifyNoInteractions(positionRepository);
    }

    @Test
    void shouldCreateNewPositionWhenTickerNotFound() {
        var investorId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var cmd = new AddPositionCommand(investorId, "VALE3", 10);

        given(assetRepository.findByTicker("VALE3")).willReturn(Optional.of(new Asset("VALE3")));

        var wallet = mock(Wallet.class);
        given(wallet.getId()).willReturn(walletId);
        given(walletRepository.findByOwnerId(investorId)).willReturn(Optional.of(wallet));

        given(positionRepository.findByWalletIdAndTickerWithLock(walletId, "VALE3"))
                .willReturn(Optional.empty());

        var newPosition = mock(Position.class);
        given(positionRepository.save(any(Position.class))).willReturn(newPosition);

        service.execute(cmd);

        verify(positionRepository, atLeastOnce()).save(any(Position.class));
        verify(newPosition).credit(cmd.quantity());
    }
}