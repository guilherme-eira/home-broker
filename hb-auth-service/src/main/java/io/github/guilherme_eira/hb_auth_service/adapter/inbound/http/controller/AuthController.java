package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto.*;
import io.github.guilherme_eira.hb_auth_service.application.dto.LoginCommand;
import io.github.guilherme_eira.hb_auth_service.application.dto.RegisterCommand;
import io.github.guilherme_eira.hb_auth_service.application.port.in.LoginUseCase;
import io.github.guilherme_eira.hb_auth_service.application.port.in.RefreshUseCase;
import io.github.guilherme_eira.hb_auth_service.application.port.in.RegisterUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para gestão de acesso, registro e renovação de tokens (Keycloak)")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final RegisterUseCase registerUseCase;

    @Operation(summary = "Realizar login", description = "Autentica o usuário no Keycloak e retorna os tokens de acesso")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (Bad Request)")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "502", description = "Erro de integração com o Keycloak")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req){
        var cmd = new LoginCommand(req.username(), req.password());
        var output = loginUseCase.execute(cmd);
        var response = new LoginResponse(output.accessToken(), output.refreshToken(), output.tokenType(), output.expiresIn());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Atualizar token (Refresh)", description = "Gera um novo Access Token utilizando um Refresh Token válido")
    @ApiResponse(responseCode = "200", description = "Token atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (Bad Request)")
    @ApiResponse(responseCode = "401", description = "Refresh Token expirado ou inválido")
    @ApiResponse(responseCode = "502", description = "Erro de integração com o Keycloak")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req){
        var output = refreshUseCase.execute(req.refreshToken());
        var response = new LoginResponse(output.accessToken(), output.refreshToken(), output.tokenType(), output.expiresIn());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Registrar novo investidor", description = "Cria um novo usuário no Keycloak e sincroniza com a base local.")
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "X-API-KEY inválida ou ausente")
    @ApiResponse(responseCode = "422", description = "Conflito de dados (User/Email/TaxId já existem)")
    @ApiResponse(responseCode = "502", description = "Erro de integração com o Keycloak")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Parameter(description = "Chave secreta para autorizar o registro", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody RegisterRequest req
    ){
        var cmd = new RegisterCommand(req.username(), req.email(), req.taxId(), req.password(), req.firstName(), req.lastName());
        var output = registerUseCase.execute(cmd);
        var response = new RegisterResponse(output.userId(), output.username(), output.createdAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
