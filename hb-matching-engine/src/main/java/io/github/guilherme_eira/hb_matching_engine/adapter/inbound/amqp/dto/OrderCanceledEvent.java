package io.github.guilherme_eira.hb_matching_engine.adapter.inbound.amqp.dto;

import java.util.UUID;

public record OrderCanceledEvent(
        UUID orderId
) {
}
