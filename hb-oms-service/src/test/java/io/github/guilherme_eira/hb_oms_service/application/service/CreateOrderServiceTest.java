package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.CreateOrderCommand;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCreatedOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.ReserveResourcePayload;
import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.out.*;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;
import io.github.guilherme_eira.hb_oms_service.domain.service.MarketSessionValidator;
import io.github.guilherme_eira.hb_oms_service.domain.vo.AssetRule;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MarketDataRepository marketDataRepository;
    @Mock
    private PortfolioServicePort portfolioService;
    @Mock
    private EventPublisherPort eventPublisher;
    @Mock
    private MarketSessionValidator marketSession;

    @InjectMocks
    private CreateOrderService service;

    @Test
    void shouldCreateLimitBidOrder() {
        var ticker = "WEGE3";
        var limitPrice = new BigDecimal("100.00");
        var quantity = 10;
        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, quantity, limitPrice, OrderType.LIMIT, OrderSide.BID);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(new BigDecimal("95.00"));
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        OrderCreatedOutput output = service.execute(cmd);

        BigDecimal expectedPrice = new BigDecimal("100.00");
        assertEquals(0, expectedPrice.compareTo(output.priceLimit()));

        var reserveCaptor = ArgumentCaptor.forClass(ReserveResourcePayload.class);
        verify(portfolioService).reserveResource(reserveCaptor.capture());

        assertEquals(0, new BigDecimal("1000.00").compareTo(reserveCaptor.getValue().volume()));
    }

    @Test
    void shouldCreateMarketBidOrder() {
        var ticker = "PETR4";
        var marketPrice = new BigDecimal("30.00");
        var quantity = 100;

        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, quantity, null, OrderType.MARKET, OrderSide.BID);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(marketPrice);
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        OrderCreatedOutput output = service.execute(cmd);

        BigDecimal expectedPrice = new BigDecimal("31.50");
        assertEquals(0, expectedPrice.compareTo(output.priceLimit()));

        var reserveCaptor = ArgumentCaptor.forClass(ReserveResourcePayload.class);
        verify(portfolioService).reserveResource(reserveCaptor.capture());

        assertEquals(0, new BigDecimal("3150.00").compareTo(reserveCaptor.getValue().volume()));
    }

    @Test
    void shouldCreateLimitAskOrder() {
        var ticker = "VALE3";
        var limitPrice = new BigDecimal("90.00");
        var quantity = 50;
        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, quantity, limitPrice, OrderType.LIMIT, OrderSide.ASK);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(new BigDecimal("91.00"));
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        OrderCreatedOutput output = service.execute(cmd);

        assertEquals(0, limitPrice.compareTo(output.priceLimit()));

        var reserveCaptor = ArgumentCaptor.forClass(ReserveResourcePayload.class);
        verify(portfolioService).reserveResource(reserveCaptor.capture());

        assertEquals(0, BigDecimal.valueOf(50).compareTo(reserveCaptor.getValue().volume()));
    }

    @Test
    void shouldCreateMarketAskOrder() {
        var ticker = "MGLU3";
        var marketPrice = new BigDecimal("2.50");
        var quantity = 1000;
        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, quantity, null, OrderType.MARKET, OrderSide.ASK);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(marketPrice);
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        OrderCreatedOutput output = service.execute(cmd);

        assertEquals(0, new BigDecimal("2.38").compareTo(output.priceLimit()));

        var reserveCaptor = ArgumentCaptor.forClass(ReserveResourcePayload.class);
        verify(portfolioService).reserveResource(reserveCaptor.capture());

        assertEquals(0, BigDecimal.valueOf(1000).compareTo(reserveCaptor.getValue().volume()));
    }

    @Test
    void shouldThrowExceptionWhenMarketSessionIsClosed() {
        willThrow(new BusinessException("Mercado fechado")).given(marketSession).validate(any());

        var cmd = new CreateOrderCommand(UUID.randomUUID(), "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.BID);

        assertThrows(BusinessException.class, () -> service.execute(cmd));
        verifyNoInteractions(marketDataRepository, orderRepository);
    }

    @Test
    void shouldThrowExceptionWhenPriceLimitTooHigh() {
        var ticker = "VALE3";
        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, 100, new BigDecimal("130.00"), OrderType.LIMIT, OrderSide.BID);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(new BigDecimal("100.00"));
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        assertThrows(BusinessException.class, () -> service.execute(cmd));
    }

    @Test
    void shouldThrowExceptionWhenPriceLimitIsTooLow() {
        var ticker = "PETR4";
        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, 100, new BigDecimal("79.00"), OrderType.LIMIT, OrderSide.ASK);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(new BigDecimal("100.00"));
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        assertThrows(BusinessException.class, () -> service.execute(cmd));
    }

    @Test
    void shouldThrowExceptionWhenAssetRuleValidationFails() {
        var ticker = "VALE3";
        var price = new BigDecimal("25.00");
        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, 100, price, OrderType.LIMIT, OrderSide.BID);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(price);
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        doThrow(new BusinessException("Preço inválido para o lote"))
                .when(assetRuleMock).validatePrice(any(BigDecimal.class));

        assertThrows(BusinessException.class, () -> service.execute(cmd));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPortfolioServiceFails() {
        var investorId = UUID.randomUUID();
        var ticker = "PETR4";
        var cmd = new CreateOrderCommand(investorId, ticker, 10, new BigDecimal("40.00"), OrderType.LIMIT, OrderSide.BID);

        var assetRuleMock = mock(AssetRule.class);
        given(assetRuleMock.referencePrice()).willReturn(new BigDecimal("38.00"));
        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.of(assetRuleMock));

        willThrow(new BusinessException("Saldo insuficiente"))
                .given(portfolioService).reserveResource(any());

        assertThrows(BusinessException.class, () -> service.execute(cmd));

        verify(eventPublisher, never()).publishOrderCreated(any());
    }

    @Test
    void shouldThrowExceptionWhenTickerNotFound() {
        var ticker = "ACAO_FANTASMA";
        var cmd = new CreateOrderCommand(UUID.randomUUID(), ticker, 100, new BigDecimal("10.00"), OrderType.LIMIT, OrderSide.BID);

        given(marketDataRepository.getAssetRule(ticker)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(cmd));
    }
}