package io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration;

import io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration.dto.KeycloakTokenResponse;
import io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration.dto.KeycloakUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "keycloak-client", url = "${keycloak.url}")
public interface KeycloakClient {

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KeycloakTokenResponse login(@RequestBody Map<String, ?> formData);

    @GetMapping("/admin/realms/${keycloak.realm}/users")
    List<Map<String, Object>> searchUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam("q") String query
    );

    @PostMapping(value = "/admin/realms/${keycloak.realm}/users",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> register(@RequestHeader("Authorization") String token, @RequestBody KeycloakUserRequest user);
}
