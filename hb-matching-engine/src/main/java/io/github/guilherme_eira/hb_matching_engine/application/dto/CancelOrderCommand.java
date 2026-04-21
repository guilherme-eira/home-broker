package io.github.guilherme_eira.hb_matching_engine.application.dto;

import java.util.UUID;

public record CancelOrderCommand(
        UUID orderId
) {
}
