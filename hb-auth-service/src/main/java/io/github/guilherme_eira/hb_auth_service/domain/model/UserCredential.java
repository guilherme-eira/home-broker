package io.github.guilherme_eira.hb_auth_service.domain.model;

public record UserCredential(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
){}