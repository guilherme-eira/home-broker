package io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration.dto;

import java.util.List;
import java.util.Map;

public record KeycloakUserRequest(
        String username,
        boolean enabled,
        boolean emailVerified,
        String email,
        String firstName,
        String lastName,
        Map<String, List<String>> attributes,
        List<Credential> credentials
) {
    public record Credential(String type, String value, boolean temporary) {}
}
