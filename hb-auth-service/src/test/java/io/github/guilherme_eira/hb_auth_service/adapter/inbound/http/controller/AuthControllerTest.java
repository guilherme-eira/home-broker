package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_auth_service.application.port.in.LoginUseCase;
import io.github.guilherme_eira.hb_auth_service.application.port.in.RefreshUseCase;
import io.github.guilherme_eira.hb_auth_service.application.port.in.RegisterUseCase;
import io.github.guilherme_eira.hb_auth_service.infra.security.SecurityConfig;
import io.github.guilherme_eira.hb_auth_service.infra.security.ApiKeyFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, ApiKeyFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RefreshUseCase refreshUseCase;

    @MockitoBean
    private RegisterUseCase registerUseCase;

    @Test
    void shouldReturn401WhenApiKeyIsWrong() throws Exception {
        String validJson = """
                {
                    "username": "guilherme",
                    "email": "gui@email.com",
                    "taxId": "226.455.480-08",
                    "password": "password123",
                    "firstName": "Guilherme",
                    "lastName": "Eira"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .header("X-API-KEY", "chave-errada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenRegisterApiKeyIsMissing() throws Exception {
        String anyJson = "{}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(anyJson))
                .andExpect(status().isUnauthorized());
    }
}