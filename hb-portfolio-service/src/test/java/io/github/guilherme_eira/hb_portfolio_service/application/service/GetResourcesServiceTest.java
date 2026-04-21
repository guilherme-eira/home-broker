package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.ResourcesOutput;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.ResourceReservationRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Position;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GetResourcesServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private ResourceReservationRepository resourceReservationRepository;

    @InjectMocks
    private GetResourcesService getResourcesService;

    @Test
    void shouldGetResourcesSuccessfully() {
        var userId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);

        var wallet = mock(Wallet.class);
        given(wallet.getId()).willReturn(walletId);
        given(wallet.getAvailableBalance()).willReturn(new BigDecimal("1500.00"));

        var position = mock(Position.class);
        given(position.getTicker()).willReturn("ITUB4");
        given(position.getQuantity()).willReturn(100);

        var blockedAssets = Map.of("ITUB4", 20);
        var totalBlockedBalance = new BigDecimal("500.00");

        given(walletRepository.findByOwnerId(userId)).willReturn(Optional.of(wallet));
        given(resourceReservationRepository.getBlockedBalance(walletId)).willReturn(totalBlockedBalance);
        given(positionRepository.findByWalletId(walletId, pageable)).willReturn(new PageImpl<>(List.of(position)));
        given(resourceReservationRepository.findAllBlockedAssets(walletId)).willReturn(blockedAssets);

        ResourcesOutput result = getResourcesService.execute(userId, pageable);

        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.availableBalance());
        assertEquals(new BigDecimal("500.00"), result.blockedBalance());

        var positionDTO = result.positions().getContent().getFirst();
        assertEquals("ITUB4", positionDTO.ticker());
        assertEquals(100, positionDTO.availableQuantity());
        assertEquals(20, positionDTO.blockedQuantity());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        var userId = UUID.randomUUID();
        given(walletRepository.findByOwnerId(userId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                getResourcesService.execute(userId, PageRequest.of(0, 10)));
    }
}