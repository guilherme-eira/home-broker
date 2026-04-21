package io.github.guilherme_eira.hb_matching_engine.application.service;

import io.github.guilherme_eira.hb_matching_engine.application.dto.CancelOrderCommand;
import io.github.guilherme_eira.hb_matching_engine.application.helper.OrderExecutionDispatcher;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.OrderBookRepository;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {

    @Mock
    private OrderBookRepository repository;

    @Mock
    private OrderExecutionDispatcher dispatcher;

    @InjectMocks
    private CancelOrderService service;

    @Test
    void shouldCancelOrderSuccessfully() {
        var orderId = UUID.randomUUID();
        var cmd = new CancelOrderCommand(orderId);

        Map<String, String> details = Map.of("id", orderId.toString(), "ticker", "WEGE3");

        var order = Order.newOrder(orderId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.BID);

        given(repository.getOrderDetails(orderId)).willReturn(details);
        given(repository.mapToOrder(details)).willReturn(order);

        service.execute(cmd);

        verify(dispatcher).dispatch(order, OrderStatus.CANCELLED);
        verify(repository).getOrderDetails(orderId);
    }

    @Test
    void shouldNotDispatchIfOrderNotFound() {
        var orderId = UUID.randomUUID();
        var cmd = new CancelOrderCommand(orderId);

        given(repository.getOrderDetails(orderId)).willReturn(Map.of());

        service.execute(cmd);

        verify(repository, never()).mapToOrder(any());
        verify(dispatcher, never()).dispatch(any(), any());
    }
}