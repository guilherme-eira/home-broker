package io.github.guilherme_eira.hb_oms_service.application.dto.input;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderCommand(
        UUID investorId,
        String ticker,
        Integer totalQuantity,
        BigDecimal priceLimit,
        OrderType type,
        OrderSide side
) {
}
