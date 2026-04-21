package io.github.guilherme_eira.hb_portfolio_service.application.dto;

import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveResourcesCommand(
        UUID investorId,
        UUID orderId,
        OrderSide side,
        String ticker,
        BigDecimal volume
) {
}
