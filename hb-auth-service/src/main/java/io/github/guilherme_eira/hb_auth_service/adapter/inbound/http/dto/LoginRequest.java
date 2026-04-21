package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessários para autenticação do usuário")
public record LoginRequest(
        @Schema(description = "Nome de usuário cadastrado", example = "guilherme_invest")
        @NotBlank(message = "O campo 'username' é obrigatório.")
        String username,

        @Schema(description = "Senha de acesso", example = "Senha@123")
        @NotBlank(message = "O campo 'password' é obrigatório.")
        String password
) {}