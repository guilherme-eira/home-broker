package io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String ticker,
        Integer totalQuantity,
        BigDecimal priceLimit,
        Integer filledQuantity,
        BigDecimal averagePrice,
        OrderType type,
        OrderSide side,
        OrderStatus status,
        Instant createdAt
) {
}
