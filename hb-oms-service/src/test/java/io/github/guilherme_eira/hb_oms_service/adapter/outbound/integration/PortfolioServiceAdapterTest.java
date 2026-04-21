package io.github.guilherme_eira.hb_oms_service.adapter.outbound.integration;

import io.github.guilherme_eira.hb_oms_service.application.dto.output.ReserveResourcePayload;
import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import io.github.guilherme_eira.hb_oms_service.application.exception.IntegrationException;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(port = 8089))
class PortfolioServiceAdapterTest {

    @Autowired
    private PortfolioServiceAdapter adapter;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Test
    void shouldReserveResourceSuccessfully() {

        mockOAuth2Token("fake-token-123");

        stubFor(post(urlEqualTo("/wallets/reserve"))
                .withHeader("Authorization", equalTo("Bearer fake-token-123"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(ok()));

        var payload = new ReserveResourcePayload(UUID.randomUUID(), UUID.randomUUID(), OrderSide.BID, null, new BigDecimal("150.00"));

        adapter.reserveResource(payload);

        verify(postRequestedFor(urlEqualTo("/wallets/reserve"))
                .withRequestBody(containing(payload.investorId().toString())));
    }

    @Test
    void shouldMapBusinessRuleViolation() {
        mockOAuth2Token("token");

        stubFor(post(urlEqualTo("/wallets/reserve"))
                .willReturn(status(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                            "code": "BUSINESS_RULE_VIOLATION",
                            "message": "Saldo insuficiente para esta operação"
                        }
                        """)));

        var payload = new ReserveResourcePayload(UUID.randomUUID(), UUID.randomUUID(), OrderSide.BID, null, new BigDecimal("150.00"));

        assertThrows(BusinessException.class, () -> adapter.reserveResource(payload));
    }

    @Test
    void shouldMapServerErrorToIntegrationException() {
        mockOAuth2Token("token");

        stubFor(post(urlEqualTo("/wallets/reserve"))
                .willReturn(serverError().withBody("{\"code\":\"ERROR\", \"message\":\"Crash\"}")));

        var payload = new ReserveResourcePayload(UUID.randomUUID(), UUID.randomUUID(), OrderSide.BID, null, new BigDecimal("150.00"));

        assertThrows(IntegrationException.class, () -> adapter.reserveResource(payload));
    }

    @Test
    void shouldHandleMalformedErrorJson() {
        mockOAuth2Token("token");

        stubFor(post(urlEqualTo("/wallets/reserve"))
                .willReturn(status(400).withBody("Isso não é um JSON")));

        var payload = new ReserveResourcePayload(UUID.randomUUID(), UUID.randomUUID(), OrderSide.BID, null, new BigDecimal("150.00"));

        assertThrows(IntegrationException.class, () -> adapter.reserveResource(payload));
    }

    private void mockOAuth2Token(String tokenValue) {
        var authorizedClient = mock(OAuth2AuthorizedClient.class);
        var accessToken = mock(OAuth2AccessToken.class);

        given(accessToken.getTokenValue()).willReturn(tokenValue);
        given(authorizedClient.getAccessToken()).willReturn(accessToken);
        given(authorizedClientManager.authorize(any())).willReturn(authorizedClient);
    }
}