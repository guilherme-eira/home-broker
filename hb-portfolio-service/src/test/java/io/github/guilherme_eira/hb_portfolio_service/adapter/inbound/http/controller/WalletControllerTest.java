package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.ReserveResourcesRequest;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.ReserveResourcesCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.GetResourcesUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.ReserveResourcesUseCase;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_portfolio_service.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@Import(SecurityConfig.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetResourcesUseCase getResourcesUseCase;

    @MockitoBean
    private ReserveResourcesUseCase reserveResourcesUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowReserveWhenHasOmsRole() throws Exception {
        var request = new ReserveResourcesRequest(
                UUID.randomUUID(), UUID.randomUUID(), OrderSide.BID, null, BigDecimal.TEN
        );

        mockMvc.perform(post("/wallets/reserve")
                        .with(jwt().authorities(new SimpleGrantedAuthority("OMS_SERVICE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(reserveResourcesUseCase).execute(any(ReserveResourcesCommand.class));
    }

    @Test
    void shouldForbiddenReserveWhenLacksOmsRole() throws Exception {
        var request = new ReserveResourcesRequest(
                UUID.randomUUID(), UUID.randomUUID(), OrderSide.BID, null, BigDecimal.TEN
        );

        mockMvc.perform(post("/wallets/reserve")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reserveResourcesUseCase);
    }
}