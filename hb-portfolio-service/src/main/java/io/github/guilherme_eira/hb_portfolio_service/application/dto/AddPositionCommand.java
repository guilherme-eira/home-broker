package io.github.guilherme_eira.hb_portfolio_service.application.dto;

import java.util.UUID;

public record AddPositionCommand(
        UUID investorId,
        String ticker,
        Integer quantity
) {
}
