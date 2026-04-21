package io.github.guilherme_eira.hb_oms_service.adapter.outbound.amqp.dto;

import java.util.UUID;

public record OrderCanceledEvent(
        UUID orderId
) {
}
