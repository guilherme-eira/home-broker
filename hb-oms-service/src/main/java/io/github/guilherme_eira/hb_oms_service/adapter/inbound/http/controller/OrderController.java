package io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto.CancelOrderRequest;
import io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto.CreateOrderRequest;
import io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto.OrderCreatedResponse;
import io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto.OrderResponse;
import io.github.guilherme_eira.hb_oms_service.adapter.outbound.mapper.OrderMapper;
import io.github.guilherme_eira.hb_oms_service.application.dto.input.CancelOrderCommand;
import io.github.guilherme_eira.hb_oms_service.application.dto.input.GetOrderByIdQuery;
import io.github.guilherme_eira.hb_oms_service.application.dto.input.GetOrdersQuery;
import io.github.guilherme_eira.hb_oms_service.application.port.in.CancelOrderUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.in.CreateOrderUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.in.GetOrderByIdUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.in.GetOrdersUseCase;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints para gerenciamento de ordens de compra e venda")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final OrderMapper mapper;

    @Operation(summary = "Cria uma nova ordem", description = "Envia uma ordem de mercado ou limitada para o sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação falhou"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: saldo insuficiente)")
    })
    @PostMapping
    public ResponseEntity<OrderCreatedResponse> create(
            @Valid @RequestBody CreateOrderRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriComponentsBuilder){

        UUID investorId = UUID.fromString(jwt.getSubject());
        var cmd = mapper.toCommand(investorId, req);
        var output = createOrderUseCase.execute(cmd);
        var uri = uriComponentsBuilder.path("/orders/{id}").buildAndExpand(output.orderId()).toUri();
        var response = mapper.toOrderCreatedResponse(output);
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Cancela uma ordem existente", description = "Solicita o cancelamento de uma ordem que ainda não foi totalmente executada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitação de cancelamento aceita"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação falhou"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: ordem já finalizada)")
    })
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@Valid @RequestBody CancelOrderRequest req, @AuthenticationPrincipal Jwt jwt){
        var investorId = UUID.fromString(jwt.getSubject());
        var cmd = new CancelOrderCommand(investorId, req.orderId());
        cancelOrderUseCase.execute(cmd);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lista as ordens do investidor", description = "Retorna uma página de ordens filtradas por status para o investidor logado")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> findAll(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Filtro opcional por status da ordem") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Número da página (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)") @RequestParam(defaultValue = "desc") String direction) {

        var sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        var pageable = PageRequest.of(page, size, sort);
        var investorId = UUID.fromString(jwt.getSubject());
        var query = new GetOrdersQuery(investorId, status);

        var output = getOrdersUseCase.execute(query, pageable);
        var response = output.map(mapper::toOrderResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca uma ordem por ID", description = "Retorna os detalhes de uma ordem específica se ela pertencer ao investidor logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem encontrada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação falhou"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou não pertence ao investidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt){
        var investorId = UUID.fromString(jwt.getSubject());
        var query = new GetOrderByIdQuery(investorId, id);

        var output = getOrderByIdUseCase.execute(query);
        var response = mapper.toOrderResponse(output);
        return ResponseEntity.ok(response);
    }
}
