package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public record ResourcesResponse(
        BigDecimal availableBalance,
        BigDecimal blockedBalance,
        Page<PositionResponseDTO> positions
) {
    public record PositionResponseDTO(
            String ticker,
            Integer availableQuantity,
            Integer blockedQuantity
    ){}
}
