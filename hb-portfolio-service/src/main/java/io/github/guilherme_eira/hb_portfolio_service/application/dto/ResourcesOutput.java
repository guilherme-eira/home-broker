package io.github.guilherme_eira.hb_portfolio_service.application.dto;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public record ResourcesOutput(
        BigDecimal availableBalance,
        BigDecimal blockedBalance,
        Page<PositionOutputDTO> positions
) {
    public record PositionOutputDTO(
            String ticker,
            Integer availableQuantity,
            Integer blockedQuantity
    ){}
}
