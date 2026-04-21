package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.amqp.dto;

import java.time.Instant;

public record UserCreatedEvent(
        String userId,
        String fullName,
        String email,
        String taxId,
        String username,
        Instant createdAt
) {
}
