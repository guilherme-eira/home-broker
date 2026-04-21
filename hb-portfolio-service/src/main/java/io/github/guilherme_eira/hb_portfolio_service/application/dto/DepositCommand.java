package io.github.guilherme_eira.hb_portfolio_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositCommand(
        UUID investorId,
        BigDecimal amount) {
}
