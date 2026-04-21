package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.DepositRequest;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.DepositCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.AddPositionUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.DepositUseCase;
import io.github.guilherme_eira.hb_portfolio_service.infra.security.ApiKeyFilter;
import io.github.guilherme_eira.hb_portfolio_service.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, ApiKeyFilter.class})
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepositUseCase depositUseCase;

    @MockitoBean
    private AddPositionUseCase addPositionUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAcceptDepositWithValidApiKey() throws Exception {
        var request = new DepositRequest(UUID.randomUUID(), new BigDecimal("500.00"));

        mockMvc.perform(post("/webhooks/deposits")
                        .header("X-API-KEY", "chave-correta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(depositUseCase).execute(any(DepositCommand.class));
    }

    @Test
    void shouldRejectWebhooksWithInvalidApiKey() throws Exception {
        var request = new DepositRequest(UUID.randomUUID(), new BigDecimal("500.00"));

        mockMvc.perform(post("/webhooks/deposits")
                        .header("X-API-KEY", "chave-errada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(depositUseCase);
        verifyNoInteractions(addPositionUseCase);
    }
}