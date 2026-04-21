package io.github.guilherme_eira.hb_matching_engine.adapter.outbound.amqp.dto;

import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderExecutedEvent(
        UUID orderId,
        OrderStatus status,
        Integer filledQuantity,
        BigDecimal averagePrice,
        List<TradeResponseDTO> trades
) {
    public record TradeResponseDTO(
            UUID tradeId,
            UUID bidOrderId,
            UUID askOrderId,
            Integer quantity,
            BigDecimal price,
            Instant executedAt
    ) {}
}
