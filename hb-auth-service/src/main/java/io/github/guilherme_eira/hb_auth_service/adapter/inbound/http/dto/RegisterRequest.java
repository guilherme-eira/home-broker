package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para criação de uma nova conta de investidor")
public record RegisterRequest(
        @Schema(description = "Nome de usuário único para acesso ao sistema", example = "guilherme_eira")
        @NotBlank(message = "O campo 'username' é obrigatório.")
        String username,

        @Schema(description = "Endereço de e-mail do investidor", example = "guilherme@exemplo.com")
        @NotBlank(message = "O campo 'email' é obrigatório.")
        @Email(message = "O e-mail informado possui um formato inválido.")
        String email,

        @Schema(description = "CPF ou CNPJ (somente números ou com máscara)", example = "123.456.789-00")
        @NotBlank(message = "O campo 'taxId' é obrigatório.")
        String taxId,

        @Schema(description = "Senha de acesso (deve seguir as políticas do Keycloak)", example = "Senha@123")
        @NotBlank(message = "O campo 'password' é obrigatório.")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "A senha deve conter ao menos uma letra maiúscula, uma minúscula, um número e um caractere especial."
        )
        String password,

        @Schema(description = "Primeiro nome do investidor", example = "Guilherme")
        @NotBlank(message = "O campo 'firstName' é obrigatório.")
        String firstName,

        @Schema(description = "Sobrenome do investidor", example = "Eira")
        @NotBlank(message = "O campo 'lastName' é obrigatório.")
        String lastName
) {}