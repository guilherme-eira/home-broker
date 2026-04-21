package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto;

import io.github.guilherme_eira.hb_portfolio_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Solicitação de reserva de saldo ou ativos para execução de ordens")
public record ReserveResourcesRequest(
        @Schema(description = "ID do investidor proprietário do recurso", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "O campo 'investorId' é obrigatório.")
        UUID investorId,

        @Schema(description = "ID da ordem gerada no OMS", example = "661f9511-f30c-52e5-b827-557766551111")
        @NotNull(message = "O campo 'orderId' é obrigatório.")
        UUID orderId,

        @Schema(description = "Lado da operação (BID para compra, ASK para venda)")
        @NotNull(message = "O campo 'side' é obrigatório.")
        OrderSide side,

        @Schema(description = "Ticker do ativo (obrigatório se type for ASK)", example = "PETR4")
        String ticker,

        @Schema(description = "Quantidade ou valor financeiro a ser bloqueado", example = "1500.00")
        @NotNull(message = "O campo 'volume' é obrigatório.")
        @Positive(message = "O campo 'volume' deve ser positivo.")
        BigDecimal volume
) {}