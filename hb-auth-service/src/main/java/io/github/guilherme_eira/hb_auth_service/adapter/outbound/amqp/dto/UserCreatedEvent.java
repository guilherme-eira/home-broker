package io.github.guilherme_eira.hb_auth_service.adapter.outbound.amqp.dto;

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
