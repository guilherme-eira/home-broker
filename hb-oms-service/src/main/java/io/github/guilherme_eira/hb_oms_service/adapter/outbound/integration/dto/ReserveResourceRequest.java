package io.github.guilherme_eira.hb_oms_service.adapter.outbound.integration.dto;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveResourceRequest(
        UUID investorId,
        UUID orderId,
        OrderSide side,
        String ticker,
        BigDecimal volume
) {
}
