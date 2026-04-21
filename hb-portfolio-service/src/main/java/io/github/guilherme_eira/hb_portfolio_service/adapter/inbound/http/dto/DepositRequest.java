package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Solicitação de aporte financeiro (dinheiro) via depósito")
public record DepositRequest(
        @Schema(description = "ID único do investidor no sistema", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "O campo 'investorId' é obrigatório.")
        UUID investorId,

        @Schema(description = "Valor financeiro a ser depositado", example = "1500.50")
        @NotNull(message = "O campo 'amount' é obrigatório.")
        @Positive(message = "O campo 'amount' deve ser maior que zero.")
        BigDecimal amount
) {}