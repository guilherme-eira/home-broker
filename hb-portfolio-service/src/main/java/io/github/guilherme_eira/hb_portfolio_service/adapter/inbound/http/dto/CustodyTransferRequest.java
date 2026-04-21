package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(description = "Solicitação de aporte de ativos (ações) via custódia externa")
public record CustodyTransferRequest(
        @Schema(description = "ID único do investidor no sistema", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "O campo 'investorId' é obrigatório.")
        UUID investorId,

        @Schema(description = "Símbolo do ativo (Ticker)", example = "PETR4")
        @NotBlank(message = "O campo 'ticker' é obrigatório.")
        String ticker,

        @Schema(description = "Quantidade de ações a serem adicionadas", example = "100")
        @NotNull(message = "O campo 'quantity' é obrigatório.")
        @Positive(message = "O campo 'quantity' deve ser maior que zero.")
        Integer quantity
) {}