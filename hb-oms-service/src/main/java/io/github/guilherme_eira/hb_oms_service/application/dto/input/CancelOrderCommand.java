package io.github.guilherme_eira.hb_oms_service.application.dto.input;

import java.util.UUID;

public record CancelOrderCommand(
        UUID investorId,
        UUID orderId
) {
}
