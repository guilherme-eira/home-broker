package io.github.guilherme_eira.hb_auth_service.application.dto;

public record RegisterCommand(
        String username,
        String email,
        String taxId,
        String password,
        String firstName,
        String lastName
) {
}
