package io.github.guilherme_eira.hb_matching_engine.adapter.inbound.amqp.dto;

import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String ticker,
        Integer totalQuantity,
        BigDecimal priceLimit,
        OrderType type,
        OrderSide side,
        Instant createdAt
) {
}
