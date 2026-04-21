package io.github.guilherme_eira.hb_oms_service.application.dto.output;

import java.util.UUID;

public record OrderCanceledOutput(
        UUID orderId
) {
}
