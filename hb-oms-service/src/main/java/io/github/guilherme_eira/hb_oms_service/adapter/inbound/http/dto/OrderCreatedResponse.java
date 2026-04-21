package io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedResponse(
        UUID orderId,
        String ticker,
        Integer totalQuantity,
        BigDecimal priceLimit,
        OrderType type,
        OrderSide side,
        Instant createdAt
) {
}
