package io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Dados para cancelamento de ordem")
public record CancelOrderRequest(

        @Schema(description = "ID da ordem", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "O campo 'orderId' é obrigatório e deve ser um UUID válido")
        UUID orderId
) {
}