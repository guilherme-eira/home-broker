package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp
) {}