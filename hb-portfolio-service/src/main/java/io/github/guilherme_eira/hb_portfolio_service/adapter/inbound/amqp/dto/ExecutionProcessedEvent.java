package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.amqp.dto;

import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ExecutionProcessedEvent(
        UUID orderId,
        String ticker,
        OrderStatus status,
        Integer filledQuantity,
        BigDecimal averagePrice
){}
