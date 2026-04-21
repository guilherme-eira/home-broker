package io.github.guilherme_eira.hb_auth_service.infra.security;

import io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${auth.security.api-key}")
    private String expectedApiKey;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().contains("/auth/register")) {
            var apiKey = request.getHeader("X-API-KEY");

            if (apiKey == null || !apiKey.equals(expectedApiKey)) {
                log.warn("Tentativa de registro bloqueada: X-API-KEY inválida ou ausente.");
                sendErrorResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        var errorResponse = new ErrorResponse(
                "AUTHENTICATION_FAILED",
                "Chave de API inválida ou ausente.",
                Instant.now()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}