package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
