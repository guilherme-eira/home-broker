package io.github.guilherme_eira.hb_oms_service.application.dto.input;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;

import java.util.UUID;

public record GetOrdersQuery(
        UUID investorId, OrderStatus status
) {
}
