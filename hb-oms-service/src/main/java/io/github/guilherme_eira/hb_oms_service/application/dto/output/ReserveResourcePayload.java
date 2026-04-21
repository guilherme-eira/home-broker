package io.github.guilherme_eira.hb_oms_service.application.dto.output;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveResourcePayload(
        UUID investorId,
        UUID orderId,
        OrderSide side,
        String ticker,
        BigDecimal volume
) {
}
