package io.github.guilherme_eira.hb_oms_service.adapter.common.dto;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp
) {}
