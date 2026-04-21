package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.CustodyTransferRequest;
import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.DepositRequest;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.AddPositionCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.DepositCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.AddPositionUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.DepositUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints de integração para entrada de recursos externos")
public class WebhookController {

    private final DepositUseCase depositUseCase;
    private final AddPositionUseCase addPositionUseCase;

    @Operation(
            summary = "Simular depósito financeiro",
            description = "Recebe uma notificação de depósito e credita o saldo na conta do investidor."
    )
    @ApiResponse(responseCode = "202", description = "Depósito processado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "X-API-KEY inválida ou ausente")
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    @PostMapping("/deposits")
    public ResponseEntity<Void> handleDeposit(
            @Parameter(description = "Chave de API para autorizar o webhook", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody DepositRequest req)
    {

        var cmd = new DepositCommand(req.investorId(), req.amount());
        depositUseCase.execute(cmd);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Simular transferência de custódia",
            description = "Recebe uma notificação de transferência de ativos e adiciona as ações à carteira do investidor."
    )
    @ApiResponse(responseCode = "202", description = "Transferência processada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "X-API-KEY inválida ou ausente")
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    @ApiResponse(responseCode = "422", description = "Ativo inexistente")
    @PostMapping("/custody-transfer")
    public ResponseEntity<Void> handleCustodyTransfer(
            @Parameter(description = "Chave de API para autorizar o webhook", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody CustodyTransferRequest req) {

        var cmd = new AddPositionCommand(req.investorId(), req.ticker(), req.quantity());
        addPositionUseCase.execute(cmd);
        return ResponseEntity.accepted().build();
    }
}
