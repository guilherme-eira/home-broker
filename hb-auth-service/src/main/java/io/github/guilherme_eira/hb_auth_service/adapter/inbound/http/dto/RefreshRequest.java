package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitação de renovação de token")
public record RefreshRequest(
        @Schema(description = "Token de renovação (refresh_token) recebido no login", example = "eyJhbGciOiJIUzI1...")
        @NotBlank(message = "O campo 'refreshToken' é obrigatório.")
        String refreshToken
) {}