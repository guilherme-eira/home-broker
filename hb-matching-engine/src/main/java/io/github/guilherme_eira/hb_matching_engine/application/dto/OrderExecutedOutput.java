package io.github.guilherme_eira.hb_matching_engine.application.dto;

import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderExecutedOutput(
        UUID orderId,
        OrderStatus status,
        Integer filledQuantity,
        BigDecimal averagePrice,
        List<TradeOutputDTO> trades
) {
    public record TradeOutputDTO(
            UUID marketTradeId,
            UUID bidOrderId,
            UUID askOrderId,
            Integer quantity,
            BigDecimal price,
            Instant executedAt
    ) {}
}
