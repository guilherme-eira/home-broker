package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.output.ExecutionProcessedOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.input.ProcessExecutionCommand;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_oms_service.application.port.out.MarketDataRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.TradeRepository;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.model.Order;
import io.github.guilherme_eira.hb_oms_service.domain.model.Trade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessExecutionServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private TradeRepository tradeRepository;
    @Mock private MarketDataRepository marketDataRepository;
    @Mock private EventPublisherPort eventPublisher;

    @InjectMocks
    private ProcessExecutionService service;

    @Test
    void shouldProcessFullExecutionSuccessfully() {
        var orderId = UUID.randomUUID();
        var ticker = "PETR4";
        var tradeId1 = UUID.randomUUID();
        var tradeId2 = UUID.randomUUID();

        var trade1 = new ProcessExecutionCommand.TradeCommandDTO(tradeId1, orderId, UUID.randomUUID(), 50, new BigDecimal("38.00"), Instant.now());
        var trade2 = new ProcessExecutionCommand.TradeCommandDTO(tradeId2, orderId, UUID.randomUUID(), 50, new BigDecimal("39.00"), Instant.now());

        var cmd = new ProcessExecutionCommand(orderId, OrderStatus.FILLED, 100, new BigDecimal("38.50"), List.of(trade1, trade2));

        var orderMock = mock(Order.class);
        given(orderMock.getTicker()).willReturn(ticker);
        given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(orderMock));

        given(tradeRepository.existsById(any())).willReturn(false);

        service.execute(cmd);

        verify(tradeRepository, times(2)).save(any(Trade.class));
        verify(orderMock).update(OrderStatus.FILLED, 100, new BigDecimal("38.50"));

        verify(marketDataRepository).updatePrice(ticker, new BigDecimal("39.00"));
        verify(eventPublisher).publishExecutionProcessed(any(ExecutionProcessedOutput.class));
    }

    @Test
    void shouldMaintainIdempotencyAndNotUpdatePriceWhenTradesAlreadyExist() {
        var orderId = UUID.randomUUID();
        var ticker = "VALE3";
        var existingTradeId = UUID.randomUUID();
        var tradeDTO = new ProcessExecutionCommand.TradeCommandDTO(existingTradeId, orderId, UUID.randomUUID(), 100, new BigDecimal("30.00"), Instant.now());
        var cmd = new ProcessExecutionCommand(orderId, OrderStatus.FILLED, 100, new BigDecimal("30.00"), List.of(tradeDTO));

        var orderMock = mock(Order.class);
        given(orderMock.getTicker()).willReturn(ticker);
        given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(orderMock));

        given(tradeRepository.existsById(existingTradeId)).willReturn(true);

        service.execute(cmd);

        verify(tradeRepository, never()).save(any(Trade.class));
        verify(marketDataRepository, never()).updatePrice(anyString(), any());
        verify(orderRepository).save(any());
    }

    @Test
    void shouldHandleCancellationWithEmptyTradesSuccessfully() {
        var orderId = UUID.randomUUID();
        var cmd = new ProcessExecutionCommand(orderId, OrderStatus.CANCELLED, 0, BigDecimal.ZERO, List.of());

        var orderMock = mock(Order.class);
        given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(orderMock));

        service.execute(cmd);

        verify(tradeRepository, never()).existsById(any());
        verify(marketDataRepository, never()).updatePrice(any(), any());
        verify(orderMock).update(eq(OrderStatus.CANCELLED), anyInt(), any());
        verify(eventPublisher).publishExecutionProcessed(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        var orderId = UUID.randomUUID();
        var cmd = new ProcessExecutionCommand(orderId, OrderStatus.FILLED, 10, BigDecimal.TEN, List.of());
        given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(cmd));
        verify(eventPublisher, never()).publishExecutionProcessed(any());
    }
}