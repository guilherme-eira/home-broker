package io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration;

import feign.FeignException;
import io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration.dto.KeycloakTokenResponse;
import io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration.dto.KeycloakUserRequest;
import io.github.guilherme_eira.hb_auth_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_auth_service.application.exception.IntegrationException;
import io.github.guilherme_eira.hb_auth_service.application.port.out.AuthServicePort;
import io.github.guilherme_eira.hb_auth_service.domain.model.User;
import io.github.guilherme_eira.hb_auth_service.domain.model.UserCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceAdapter implements AuthServicePort {
    private final KeycloakClient client;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Override
    public UserCredential login(String username, String password) {
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "password");
            formData.put("client_id", clientId);
            formData.put("client_secret", clientSecret);
            formData.put("username", username);
            formData.put("password", password);

            return toDomain(client.login(formData));
        } catch (FeignException.Unauthorized ex) {
            throw new BadCredentialsException("Usuário ou senha inválidos.");
        } catch (FeignException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }

    @Override
    public UserCredential refresh(String refreshToken) {
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "refresh_token");
            formData.put("client_id", clientId);
            formData.put("client_secret", clientSecret);
            formData.put("refresh_token", refreshToken);

            KeycloakTokenResponse response = client.login(formData);

            return toDomain(response);
        } catch (FeignException.BadRequest ex) {
            String content = ex.contentUTF8();
            if (content != null && content.contains("invalid_grant")) {
                throw new BadCredentialsException("Sessão expirada ou token inválido.");
            }
            throw new IntegrationException(ex.getMessage());
        }catch (FeignException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }

    @Override
    public String register(User user) {
        try {
            Map<String, String> authData = Map.of(
                    "grant_type", "client_credentials",
                    "client_id", clientId,
                    "client_secret", clientSecret
            );
            String adminToken = "Bearer " + client.login(authData).accessToken();

            String query = "taxId:" + user.taxId();
            var existingUsers = client.searchUsers(adminToken, query);

            if (existingUsers != null && !existingUsers.isEmpty()) {
                throw new BusinessException("Informações já estão sendo usadas por outro usuário.");
            }

            var keycloakUser = new KeycloakUserRequest(
                    user.username(),
                    true,
                    true,
                    user.email(),
                    user.firstName(),
                    user.lastName(),
                    Map.of("taxId", List.of(user.taxId())),
                    List.of(new KeycloakUserRequest.Credential("password", user.password(), false))
            );

            var response = client.register(adminToken, keycloakUser);

            var locationHeader = response.getHeaders().getLocation();

            if (locationHeader == null) {
                throw new IntegrationException("Keycloak não retornou o cabeçalho Location com o ID do usuário.");
            }
            String locationPath = locationHeader.getPath();
            return locationPath.substring(locationPath.lastIndexOf("/") + 1);

        } catch (FeignException.Conflict ex) {
            throw new BusinessException("Informações já estão sendo usadas por outro usuário.");

        } catch (FeignException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }

    private UserCredential toDomain(KeycloakTokenResponse response) {
        return new UserCredential(
                response.accessToken(),
                response.refreshToken(),
                response.tokenType(),
                response.expiresIn()
        );
    }
}