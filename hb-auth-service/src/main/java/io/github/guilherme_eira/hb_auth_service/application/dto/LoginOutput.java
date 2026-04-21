package io.github.guilherme_eira.hb_auth_service.application.dto;

public record LoginOutput(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
