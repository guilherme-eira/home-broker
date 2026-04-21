package io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_auth_service.adapter.inbound.http.dto.ErrorResponse;
import io.github.guilherme_eira.hb_auth_service.application.exception.IntegrationException;
import io.github.guilherme_eira.hb_auth_service.domain.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn("Falha de validação nos dados de entrada: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETERS", message);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentativa de acesso não autorizada: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Violação de regra de negócio: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", ex.getMessage());
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrationException(IntegrationException ex) {
        log.error("Falha na comunicação com o servidor de identidade: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_GATEWAY, "SERVICE_UNAVAILABLE",
                "Não foi possível completar a operação com o provedor de identidade.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro interno não tratado no Auth Service: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Erro inesperado no servidor. Tente novamente mais tarde.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message) {
        var error = new ErrorResponse(code, message, Instant.now());
        return ResponseEntity.status(status).body(error);
    }
}