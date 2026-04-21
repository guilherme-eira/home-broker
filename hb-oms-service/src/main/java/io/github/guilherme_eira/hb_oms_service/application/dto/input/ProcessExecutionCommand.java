package io.github.guilherme_eira.hb_oms_service.application.dto.input;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProcessExecutionCommand(
        UUID orderId,
        OrderStatus status,
        Integer filledQuantity,
        BigDecimal averagePrice,
        List<TradeCommandDTO> trades
) {
    public record TradeCommandDTO(
            UUID tradeId,
            UUID bidId,
            UUID askId,
            Integer quantity,
            BigDecimal price,
            Instant executedAt
    ) {}
}

