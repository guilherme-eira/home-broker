package io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("token_type") String tokenType
) {
}
