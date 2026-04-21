package io.github.guilherme_eira.hb_oms_service.adapter.inbound.amqp.dto;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderExecutedEvent(
        UUID orderId,
        OrderStatus status,
        Integer filledQuantity,
        BigDecimal averagePrice,
        List<TradeEventDTO> trades
) {
    public record TradeEventDTO(
            UUID tradeId,
            UUID bidOrderId,
            UUID askOrderId,
            Integer quantity,
            BigDecimal price,
            Instant executedAt
    ) {}
}

