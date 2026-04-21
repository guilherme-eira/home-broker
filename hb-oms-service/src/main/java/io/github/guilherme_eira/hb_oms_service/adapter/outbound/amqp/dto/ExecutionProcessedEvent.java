package io.github.guilherme_eira.hb_oms_service.adapter.outbound.amqp.dto;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ExecutionProcessedEvent(
        UUID orderId,
        String ticker,
        OrderStatus status,
        Integer filledQuantity,
        BigDecimal averagePrice
) {
}
