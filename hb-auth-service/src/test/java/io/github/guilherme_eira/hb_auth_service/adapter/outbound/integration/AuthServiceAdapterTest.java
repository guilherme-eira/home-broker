package io.github.guilherme_eira.hb_auth_service.adapter.outbound.integration;

import io.github.guilherme_eira.hb_auth_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_auth_service.application.exception.IntegrationException;
import io.github.guilherme_eira.hb_auth_service.domain.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(port = 8089))
class AuthServiceAdapterTest {

    @Autowired
    private AuthServiceAdapter adapter;

    @Test
    void shouldLoginSuccessfully() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .withRequestBody(containing("grant_type=password"))
                .withRequestBody(containing("username=guilherme"))
                .withRequestBody(containing("password=senha123"))
                .willReturn(okJson("""
                {
                    "access_token": "access-123",
                    "refresh_token": "refresh-456"
                }
                """)));

        var result = adapter.login("guilherme", "senha123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("access-123", result.accessToken());
        Assertions.assertEquals("refresh-456", result.refreshToken());
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenUnauthorized() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(unauthorized()));

        Assertions.assertThrows(BadCredentialsException.class,
                () -> adapter.login("usuario_errado", "senha_errada"));
    }

    @Test
    void shouldThrowIntegrationExceptionOnLoginServerError() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(serverError()));

        Assertions.assertThrows(IntegrationException.class,
                () -> adapter.login("guilherme", "senha123"));
    }

    @Test
    void shouldRefreshSuccessfully() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .withRequestBody(containing("grant_type=refresh_token"))
                .withRequestBody(containing("refresh_token=old-refresh-token"))
                .willReturn(okJson("""
                {
                    "access_token": "new-access-123",
                    "refresh_token": "new-refresh-456"
                }
                """)));

        var result = adapter.refresh("old-refresh-token");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("new-access-123", result.accessToken());
        Assertions.assertEquals("new-refresh-456", result.refreshToken());
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRefreshTokenIsInvalid() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"invalid_grant\", \"error_description\": \"Token expired\"}")));

        Assertions.assertThrows(BadCredentialsException.class,
                () -> adapter.refresh("token-expirado"));
    }

    @Test
    void shouldThrowIntegrationExceptionOnOtherBadRequest() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(badRequest()
                        .withBody("{\"error\": \"invalid_client\"}")));

        Assertions.assertThrows(IntegrationException.class,
                () -> adapter.refresh("algum-token"));
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(okJson("""
                        {
                            "access_token":"token-admin-123",
                            "token_type":"Bearer"
                        }
                        """)));

        stubFor(get(urlPathEqualTo("/admin/realms/home-broker/users"))
                .withQueryParam("q", equalTo("taxId:12345678900"))
                .withHeader("Authorization", equalTo("Bearer token-admin-123"))
                .willReturn(okJson("[]")));

        stubFor(post(urlEqualTo("/admin/realms/home-broker/users"))
                .withHeader("Authorization", equalTo("Bearer token-admin-123"))
                .willReturn(created()
                        .withHeader("Location", "http://localhost:8080/admin/realms/home-broker/users/new-uuid-789")));

        String userId = adapter.register(createUser());

        Assertions.assertEquals("new-uuid-789", userId);
    }

    @Test
    void shouldThrowExceptionWhenTaxIdExists() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(okJson("""
                {
                    "access_token":"abc"
                }
                """)));

        stubFor(get(urlPathEqualTo("/admin/realms/home-broker/users"))
                .willReturn(okJson("""
                        [
                            {
                                "id":"123", "username":"outro_user"
                            }
                        ]
                        """)));

        User user = createUser();

        Assertions.assertThrows(BusinessException.class, () -> adapter.register(user));
    }

    @Test
    void shouldThrowBusinessExceptionWhenKeycloakReturnsConflict() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(okJson("""
                        {
                            "access_token":"admin-token"
                        }
                        """)));
        stubFor(get(urlPathEqualTo("/admin/realms/home-broker/users"))
                .willReturn(okJson("[]")));

        stubFor(post(urlEqualTo("/admin/realms/home-broker/users"))
                .willReturn(status(409)));

        User user = createUser();

        Assertions.assertThrows(BusinessException.class, () -> adapter.register(user));
    }

    @Test
    void shouldThrowIntegrationExceptionWhenLocationHeaderIsMissing() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token")).willReturn(okJson("{access_token:admin-token}")));
        stubFor(get(urlPathEqualTo("/admin/realms/home-broker/users")).willReturn(okJson("[]")));

        stubFor(post(urlEqualTo("/admin/realms/home-broker/users"))
                .willReturn(created()));

        User user = createUser();

        Assertions.assertThrows(IntegrationException.class, () -> adapter.register(user));
    }

    @Test
    void shouldThrowIntegrationExceptionOnKeycloakError() {
        stubFor(post(urlEqualTo("/realms/home-broker/protocol/openid-connect/token"))
                .willReturn(serverError()));

        User user = createUser();

        Assertions.assertThrows(IntegrationException.class, () -> adapter.register(user));
    }

    private User createUser(){
        return new User("guilherme", "gui@email.com", "12345678900", "Gui", "Eira", "senha123");
    }

}