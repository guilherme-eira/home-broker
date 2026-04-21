package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.ReserveResourcesRequest;
import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.ResourcesResponse;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.ReserveResourcesCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.ResourcesOutput;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.GetResourcesUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.ReserveResourcesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Gestão de recursos financeiro e custódia de ativos")
public class WalletController {

    private final GetResourcesUseCase getResourcesUseCase;
    private final ReserveResourcesUseCase reserveResourcesUseCase;

    @Operation(
            summary = "Consultar recursos do investidor",
            description = "Retorna o saldo (disponível/bloqueado) e as posições em custódia do usuário logado via JWT."
    )
    @ApiResponse(responseCode = "200", description = "Recursos recuperados com sucesso")
    @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    @GetMapping("/me/resources")
    public ResponseEntity<ResourcesResponse> getResources(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Número da página (0..N)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação", example = "ticker")
            @RequestParam(defaultValue = "ticker") String sortBy,
            @Parameter(description = "Direção da ordenação", example = "asc")
            @RequestParam(defaultValue = "asc") String direction
    ){
        var sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        var pageable = PageRequest.of(page, size, sort);
        var userId = UUID.fromString(jwt.getSubject());

        var output = getResourcesUseCase.execute(userId, pageable);
        return ResponseEntity.ok().body(toResponse(output));
    }

    @Operation(
            summary = "Reservar recursos para ordem",
            description = "Bloqueia saldo ou ações para garantir a execução de uma nova ordem emitida pelo OMS."
    )
    @ApiResponse(responseCode = "204", description = "Recurso reservado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (Bad Request)")
    @ApiResponse(responseCode = "401", description = "Autenticação falhou (token inválido ou ausente)")
    @ApiResponse(responseCode = "403", description = "Acesso negado (permissão insuficiente)")
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    @ApiResponse(responseCode = "422", description = "Saldo ou custódia insuficiente para realizar a reserva")
    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody ReserveResourcesRequest req){
        var cmd = new ReserveResourcesCommand(req.investorId(), req.orderId(), req.side(), req.ticker(), req.volume());
        reserveResourcesUseCase.execute(cmd);
        return ResponseEntity.noContent().build();
    }

    private ResourcesResponse toResponse(ResourcesOutput output){
        return new ResourcesResponse(
                output.availableBalance(),
                output.blockedBalance(),
                output.positions().map(p -> {
                    return new ResourcesResponse.PositionResponseDTO(
                            p.ticker(),
                            p.availableQuantity(),
                            p.blockedQuantity()
                    );
                })
        );
    }
}
