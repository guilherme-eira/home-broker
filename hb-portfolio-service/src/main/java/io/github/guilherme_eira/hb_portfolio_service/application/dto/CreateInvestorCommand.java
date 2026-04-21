package io.github.guilherme_eira.hb_portfolio_service.application.dto;

import java.time.Instant;

public record CreateInvestorCommand(
        String userId,
        String fullName,
        String email,
        String taxId,
        String username,
        Instant createdAt
) {
}
