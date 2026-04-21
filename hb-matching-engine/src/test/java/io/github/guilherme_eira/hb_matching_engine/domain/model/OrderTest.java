package io.github.guilherme_eira.hb_matching_engine.domain.model;

import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest {

    @ParameterizedTest
    @CsvSource({
            "BID, 35.00, 34.50, true",
            "BID, 35.00, 35.00, true",
            "BID, 35.00, 35.01, false",
            "ASK, 30.00, 30.50, true",
            "ASK, 30.00, 30.00, true",
            "ASK, 30.00, 29.99, false"
    })
    void shouldValidateCanMatchWith(OrderSide side, String limit, String counterParty, boolean expected) {
        var order = Order.newOrder(UUID.randomUUID(), "PETR4", 100, new BigDecimal(limit), OrderType.LIMIT, side);
        assertEquals(expected, order.canMatchWith(new BigDecimal(counterParty)));
    }

    @Test
    void shouldCalculateAveragePriceCorrectly() {
        var order = Order.newOrder(UUID.randomUUID(), "WEGE3", 100, new BigDecimal("40.00"), OrderType.LIMIT, OrderSide.BID);

        order.addTrade(Trade.create(order.getId(), UUID.randomUUID(), new BigDecimal("38.00"), 40));
        order.addToFilledQuantity(40);

        order.addTrade(Trade.create(order.getId(), UUID.randomUUID(), new BigDecimal("39.00"), 60));
        order.addToFilledQuantity(60);

        BigDecimal averagePrice = order.calculateAveragePrice();

        assertEquals(0, new BigDecimal("38.60").compareTo(averagePrice));
    }

    @Test
    void shouldReturnZeroAveragePriceWhenNoTradesExist() {
        var order = Order.newOrder(UUID.randomUUID(), "VALE3", 100, new BigDecimal("90.00"), OrderType.LIMIT, OrderSide.ASK);
        assertEquals(BigDecimal.ZERO, order.calculateAveragePrice());
    }

    @Test
    void shouldAccumulateFilledQuantity() {
        var order = Order.newOrder(UUID.randomUUID(), "MGLU3", 1000, new BigDecimal("2.50"), OrderType.LIMIT, OrderSide.BID);

        order.addToFilledQuantity(200);
        order.addToFilledQuantity(150);

        assertEquals(350, order.getFilledQuantity());
    }
}