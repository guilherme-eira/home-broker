package io.github.guilherme_eira.hb_matching_engine.application.helper;

import io.github.guilherme_eira.hb_matching_engine.application.dto.OrderExecutedOutput;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.OrderBookRepository;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Order;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Trade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderExecutionDispatcherTest {

    @Mock
    private OrderBookRepository repository;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private OrderExecutionDispatcher dispatcher;

    @Captor
    private ArgumentCaptor<OrderExecutedOutput> outputCaptor;

    private final UUID orderId = UUID.randomUUID();
    private final String ticker = "WEGE3";

    @Test
    void shouldDispatchFilledOrderAndRemoveFromRepository() {

        var trade = Trade.create(orderId, UUID.randomUUID(), new BigDecimal("35.00"), 100);

        var order = Order.newOrder(
                orderId,
                ticker,
                100,
                new BigDecimal("35.00"),
                OrderType.LIMIT,
                OrderSide.BID
        );
        order.addTrade(trade);
        order.addToFilledQuantity(100);

        dispatcher.dispatch(order, OrderStatus.FILLED);

        verify(repository).removeOrder(orderId, ticker, OrderSide.BID);
        verify(eventPublisher).publishOrderProcessed(outputCaptor.capture());

        OrderExecutedOutput captured = outputCaptor.getValue();
        assertEquals(orderId, captured.orderId());
        assertEquals(OrderStatus.FILLED, captured.status());
        assertEquals(100, captured.filledQuantity());
        assertEquals(0, new BigDecimal("35.00").compareTo(captured.averagePrice()));
    }

    @Test
    void shouldDispatchPartialOrderAndKeepInRepository() {

        var trade = Trade.create(orderId, UUID.randomUUID(), new BigDecimal("35.00"), 60);

        var order = Order.newOrder(
                orderId,
                ticker,
                100,
                new BigDecimal("35.00"),
                OrderType.LIMIT,
                OrderSide.BID
        );
        order.addTrade(trade);
        order.addToFilledQuantity(60);

        dispatcher.dispatch(order, OrderStatus.PARTIAL);

        verify(repository, never()).removeOrder(any(), any(), any());
        verify(eventPublisher).publishOrderProcessed(outputCaptor.capture());

        OrderExecutedOutput captured = outputCaptor.getValue();
        assertEquals(orderId, captured.orderId());
        assertEquals(OrderStatus.PARTIAL, captured.status());
        assertEquals(60, captured.filledQuantity());
        assertEquals(0, new BigDecimal("35.00").compareTo(captured.averagePrice()));
    }

    @Test
    void shouldRemoveFromBookWhenCancelled() {
        var order = Order.newOrder(
                orderId,
                ticker,
                100,
                new BigDecimal("35.00"),
                OrderType.LIMIT,
                OrderSide.BID
        );

        dispatcher.dispatch(order, OrderStatus.CANCELLED);

        verify(repository).removeOrder(orderId, ticker, OrderSide.BID);
        verify(eventPublisher).publishOrderProcessed(outputCaptor.capture());

        assertEquals(OrderStatus.CANCELLED, outputCaptor.getValue().status());
    }
}