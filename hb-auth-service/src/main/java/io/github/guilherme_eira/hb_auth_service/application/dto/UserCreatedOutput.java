package io.github.guilherme_eira.hb_auth_service.application.dto;

import java.time.Instant;

public record UserCreatedOutput(
        String userId,
        String fullName,
        String email,
        String taxId,
        String username,
        Instant createdAt
) {
}
