package io.github.guilherme_eira.hb_portfolio_service.infra.security;

import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${webhook.security.api-key}")
    private String expectedApiKey;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/webhooks")) {
            var apiKey = request.getHeader("X-API-KEY");

            if (apiKey == null || !apiKey.equals(expectedApiKey)) {
                sendErrorResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        var errorResponse = new ErrorResponse(
                "AUTHENTICATION_FAILED",
                "Chave de API inválida ou ausente para integração de Webhook.",
                Instant.now()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
