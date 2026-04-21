package io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Representação dos dados necessários para registrar uma nova ordem no sistema")
public record CreateOrderRequest(

        @Schema(description = "Símbolo do ativo (Ticker)", example = "WEGE3")
        @NotBlank(message = "O campo 'ticker' é obrigatório")
        String ticker,

        @Schema(description = "Quantidade total de ativos da ordem", example = "100")
        @NotNull(message = "O campo 'totalQuantity' é obrigatório")
        @Positive(message = "O campo 'totalQuantity' deve ser um valor positivo maior que zero")
        Integer totalQuantity,

        @Schema(description = "Preço limite para execução da ordem", example = "38.50")
        @Positive(message = "O campo 'priceLimit' deve ser um valor positivo maior que zero")
        BigDecimal priceLimit,

        @Schema(description = "Tipo da ordem (ex: LIMIT, MARKET)", example = "LIMIT")
        @NotNull(message = "O campo 'type' é obrigatório")
        OrderType type,

        @Schema(description = "Lado da operação (BID para compra, ASK para venda)", example = "BID")
        @NotNull(message = "O campo 'side' é obrigatório")
        OrderSide side
) {
}