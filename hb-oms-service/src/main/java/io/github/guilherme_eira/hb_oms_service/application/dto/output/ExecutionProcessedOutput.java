package io.github.guilherme_eira.hb_oms_service.application.dto.output;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ExecutionProcessedOutput(
        UUID orderId,
        String ticker,
        OrderStatus status,
        Integer filledQuantity,
        BigDecimal averagePrice
) {}