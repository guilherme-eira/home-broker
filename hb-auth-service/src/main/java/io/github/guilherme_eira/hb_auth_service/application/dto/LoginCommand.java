package io.github.guilherme_eira.hb_auth_service.application.dto;

public record LoginCommand(
        String username,
        String password
) {
}
