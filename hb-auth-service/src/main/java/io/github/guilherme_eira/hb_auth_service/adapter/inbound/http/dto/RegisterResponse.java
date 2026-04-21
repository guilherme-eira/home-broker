package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto;

import java.time.Instant;

public record RegisterResponse(
        String id,
        String username,
        Instant createdAt
) {
}
