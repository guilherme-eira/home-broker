package io.github.guilherme_eira.hb_matching_engine.application.service;

import io.github.guilherme_eira.hb_matching_engine.application.dto.ExecuteOrderCommand;
import io.github.guilherme_eira.hb_matching_engine.application.helper.OrderExecutionDispatcher;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.OrderBookRepository;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteOrderServiceTest {

    @Mock private OrderBookRepository repository;
    @Mock private OrderExecutionDispatcher dispatcher;

    @InjectMocks
    private ExecuteOrderService service;

    @Captor private ArgumentCaptor<Order> orderCaptor;
    @Captor private ArgumentCaptor<OrderStatus> statusCaptor;

    @Test
    void shouldExecuteFullImmediateMatch() {
        var bidId = UUID.randomUUID();
        var askId = UUID.randomUUID();
        var cmd = new ExecuteOrderCommand(bidId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT,
                OrderSide.BID, Instant.now());

        var askOrder = Order.newOrder(askId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.ASK);
        Map<String, String> askDetails = Map.of("id", askId.toString());

        given(repository.getBestOfferId("WEGE3", OrderSide.BID)).willReturn(askId.toString());
        given(repository.getOrderDetails(askId)).willReturn(askDetails);
        given(repository.mapToOrder(askDetails)).willReturn(askOrder);

        service.execute(cmd);

        verify(dispatcher, times(2)).dispatch(orderCaptor.capture(), statusCaptor.capture());

        List<Order> capturedOrders = orderCaptor.getAllValues();
        List<OrderStatus> capturedStatuses = statusCaptor.getAllValues();

        assertTrue(capturedOrders.stream().anyMatch(o -> o.getId().equals(bidId)));
        assertTrue(capturedOrders.stream().anyMatch(o -> o.getId().equals(askId)));
        assertTrue(capturedStatuses.stream().allMatch(s -> s == OrderStatus.FILLED));

        verify(repository, never()).saveToBook(any());
    }

    @Test
    void shouldSweepTheBookWithMultipleCounterparties() {
        var bidId = UUID.randomUUID();
        var askId1 = UUID.randomUUID();
        var askId2 = UUID.randomUUID();
        var cmd = new ExecuteOrderCommand(bidId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT,
                OrderSide.BID, Instant.now());

        var ask1 = Order.newOrder(askId1, "WEGE3", 50, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.ASK);
        var ask2 = Order.newOrder(askId2, "WEGE3", 50, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.ASK);

        Map<String, String> details1 = Map.of("id", askId1.toString());
        Map<String, String> details2 = Map.of("id", askId2.toString());

        given(repository.getBestOfferId("WEGE3", OrderSide.BID))
                .willReturn(askId1.toString())
                .willReturn(askId2.toString())
                .willReturn(null);

        given(repository.getOrderDetails(askId1)).willReturn(details1);
        given(repository.getOrderDetails(askId2)).willReturn(details2);
        given(repository.mapToOrder(details1)).willReturn(ask1);
        given(repository.mapToOrder(details2)).willReturn(ask2);

        service.execute(cmd);

        verify(dispatcher, times(3)).dispatch(orderCaptor.capture(), statusCaptor.capture());

        var orders = orderCaptor.getAllValues();
        assertEquals(3, orders.size());
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(askId1)));
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(askId2)));
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(bidId)));
        assertTrue(statusCaptor.getAllValues().stream().allMatch(s -> s == OrderStatus.FILLED));
    }

    @Test
    void shouldExecuteWithPriceImprovementForIncome() {
        var bidId = UUID.randomUUID();
        var askId = UUID.randomUUID();
        var priceLimit = new BigDecimal("10.00");
        var marketPrice = new BigDecimal("9.50");
        var cmd = new ExecuteOrderCommand(bidId, "PETR4", 100, priceLimit, OrderType.LIMIT, OrderSide.BID, Instant.now());

        var askOrder = Order.newOrder(askId, "PETR4", 100, marketPrice, OrderType.LIMIT, OrderSide.ASK);

        given(repository.getBestOfferId("PETR4", OrderSide.BID)).willReturn(askId.toString());
        given(repository.getOrderDetails(askId)).willReturn(Map.of("any", "data"));
        given(repository.mapToOrder(any())).willReturn(askOrder);

        service.execute(cmd);

        verify(dispatcher, atLeastOnce()).dispatch(orderCaptor.capture(), any());

        Order incomeOrder = orderCaptor.getAllValues().stream()
                .filter(o -> o.getId().equals(bidId))
                .findFirst()
                .orElseThrow();

        BigDecimal actualTradePrice = incomeOrder.getTrades().getFirst().getPrice();
        assertEquals(0, marketPrice.compareTo(actualTradePrice), "O preço do trade deve ser o melhor preço disponível no book");
    }

    @Test
    void shouldExecuteWhenIncomeIsLargerThanCounterParty() {
        var bidId = UUID.randomUUID();
        var askId = UUID.randomUUID();
        var cmd = new ExecuteOrderCommand(bidId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT,
                OrderSide.BID, Instant.now());

        var askOrder = Order.newOrder(askId, "WEGE3", 40, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.ASK);

        given(repository.getBestOfferId("WEGE3", OrderSide.BID)).willReturn(askId.toString()).willReturn(null);
        given(repository.getOrderDetails(askId)).willReturn(Map.of("id", askId.toString()));
        given(repository.mapToOrder(any())).willReturn(askOrder);

        service.execute(cmd);

        verify(dispatcher, times(2)).dispatch(orderCaptor.capture(), statusCaptor.capture());

        Order capturedAsk = orderCaptor.getAllValues().stream()
                .filter(o -> o.getId().equals(askId)).findFirst().orElseThrow();
        assertEquals(40, capturedAsk.getFilledQuantity());
        assertEquals(OrderStatus.FILLED, statusCaptor.getAllValues().getFirst());

        Order capturedBid = orderCaptor.getAllValues().stream()
                .filter(o -> o.getId().equals(bidId)).findFirst().orElseThrow();
        assertEquals(40, capturedBid.getFilledQuantity());
        assertEquals(OrderStatus.PARTIAL, statusCaptor.getAllValues().get(1));

        verify(repository).saveToBook(argThat(o -> o.getId().equals(bidId) && o.getFilledQuantity() == 40));
    }

    @Test
    void shouldExecuteWhenIncomeIsSmallerThanCounterparty() {
        var bidId = UUID.randomUUID();
        var askId = UUID.randomUUID();
        var cmd = new ExecuteOrderCommand(bidId, "WEGE3", 40, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.BID
                , Instant.now());

        var askOrder = Order.newOrder(askId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.ASK);

        given(repository.getBestOfferId("WEGE3", OrderSide.BID)).willReturn(askId.toString());
        given(repository.getOrderDetails(askId)).willReturn(Map.of("id", askId.toString()));
        given(repository.mapToOrder(any())).willReturn(askOrder);

        service.execute(cmd);

        verify(dispatcher, times(2)).dispatch(orderCaptor.capture(), statusCaptor.capture());

        Order capturedBid = orderCaptor.getAllValues().stream()
                .filter(o -> o.getId().equals(bidId)).findFirst().orElseThrow();
        assertEquals(40, capturedBid.getFilledQuantity());
        assertEquals(OrderStatus.FILLED, statusCaptor.getAllValues().get(1));

        Order capturedAsk = orderCaptor.getAllValues().stream()
                .filter(o -> o.getId().equals(askId)).findFirst().orElseThrow();
        assertEquals(40, capturedAsk.getFilledQuantity());
        assertEquals(OrderStatus.PARTIAL, statusCaptor.getAllValues().getFirst());

        verify(repository).saveToBook(argThat(o -> o.getId().equals(askId) && o.getFilledQuantity() == 40));
    }


    @Test
    void marketOrderShouldExpireIfNoLiquidity() {
        var orderId = UUID.randomUUID();
        var cmd = new ExecuteOrderCommand(
                orderId, "PETR4", 100, null, OrderType.MARKET, OrderSide.BID, Instant.now()
        );

        given(repository.getBestOfferId("PETR4", OrderSide.BID)).willReturn(null);

        service.execute(cmd);

        verify(dispatcher).dispatch(orderCaptor.capture(), statusCaptor.capture());

        assertEquals(OrderStatus.EXPIRED, statusCaptor.getValue());
        assertEquals(orderId, orderCaptor.getValue().getId());

        verify(repository, never()).saveToBook(any());
    }

    @Test
    void shouldNotMatchWhenPricesAreIncompatible() {
        var bidId = UUID.randomUUID();
        var askId = UUID.randomUUID();

        var cmd = new ExecuteOrderCommand(bidId, "VALE3", 100, new BigDecimal("10.00"), OrderType.LIMIT, OrderSide.BID, Instant.now());

        var askOrder = Order.newOrder(askId, "VALE3", 100, new BigDecimal("10.01"), OrderType.LIMIT, OrderSide.ASK);

        given(repository.getBestOfferId("VALE3", OrderSide.BID)).willReturn(askId.toString());
        given(repository.getOrderDetails(askId)).willReturn(Map.of("id", askId.toString()));
        given(repository.mapToOrder(any())).willReturn(askOrder);

        service.execute(cmd);

        verify(dispatcher, never()).dispatch(any(), any());

        verify(repository).saveToBook(argThat(o -> o.getId().equals(bidId) && o.getFilledQuantity() == 0));
    }

    @Test
    void limitOrderShouldGoToBookIfNoLiquidity() {
        var orderId = UUID.randomUUID();
        var cmd = new ExecuteOrderCommand(
                orderId, "VALE3", 50, new BigDecimal("90.00"), OrderType.LIMIT, OrderSide.BID, Instant.now());

        given(repository.getBestOfferId("VALE3", OrderSide.BID)).willReturn(null);

        service.execute(cmd);

        verify(dispatcher, never()).dispatch(any(), any());
        verify(repository).saveToBook(argThat(o -> o.getId().equals(orderId) && o.getFilledQuantity() == 0));
    }

    @Test
    void marketOrderShouldExecutePartiallyAndExpireRemainder() {
        var marketOrderId = UUID.randomUUID();
        var askId = UUID.randomUUID();
        var cmd = new ExecuteOrderCommand(
                marketOrderId, "ITUB4", 100, new BigDecimal("25.00"), OrderType.MARKET, OrderSide.BID, Instant.now());

        var askOrder = Order.newOrder(askId, "ITUB4", 30, new BigDecimal("25.00"), OrderType.LIMIT, OrderSide.ASK);

        given(repository.getBestOfferId("ITUB4", OrderSide.BID)).willReturn(askId.toString()).willReturn(null);
        given(repository.getOrderDetails(askId)).willReturn(Map.of("id", askId.toString()));
        given(repository.mapToOrder(any())).willReturn(askOrder);

        service.execute(cmd);

        verify(dispatcher, times(2)).dispatch(orderCaptor.capture(), statusCaptor.capture());

        assertEquals(OrderStatus.FILLED, statusCaptor.getAllValues().get(0));

        assertEquals(OrderStatus.EXPIRED, statusCaptor.getAllValues().get(1));
        assertEquals(30, orderCaptor.getAllValues().get(1).getFilledQuantity());

        verify(repository, never()).saveToBook(argThat(o -> o.getType() == OrderType.MARKET));
    }

    @Test
    void shouldHandleGhostOrderAndContinueMatching() {
        var bidId = UUID.randomUUID();
        var ghostId = UUID.randomUUID();
        var realAskId = UUID.randomUUID();

        var cmd = new ExecuteOrderCommand(bidId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT,
                OrderSide.BID, Instant.now());
        var realAsk = Order.newOrder(realAskId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.ASK);

        given(repository.getBestOfferId("WEGE3", OrderSide.BID))
                .willReturn(ghostId.toString())
                .willReturn(realAskId.toString())
                .willReturn(null);

        given(repository.getOrderDetails(ghostId)).willReturn(Map.of());
        given(repository.getOrderDetails(realAskId)).willReturn(Map.of("id", realAskId.toString()));
        given(repository.mapToOrder(any())).willReturn(realAsk);

        service.execute(cmd);

        verify(repository).removeOrder(ghostId, "WEGE3", OrderSide.ASK);

        verify(dispatcher, times(2)).dispatch(orderCaptor.capture(), any());
        assertTrue(orderCaptor.getAllValues().stream().anyMatch(o -> o.getId().equals(realAskId)));
        assertTrue(orderCaptor.getAllValues().stream().anyMatch(o -> o.getId().equals(bidId)));
    }
}