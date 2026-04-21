package io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_oms_service.adapter.common.dto.ErrorResponse;
import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import io.github.guilherme_eira.hb_oms_service.application.exception.IntegrationException;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex) {
        log.error("Tentativa de acesso não autorizada: {}", ex.getMessage());
        var error = new ErrorResponse("UNAUTHORIZED", "Acesso negado. Por favor, faça login novamente.", Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn("Falha de validação na requisição: {}", message);
        var error = new ErrorResponse("INVALID_PARAMETERS", message, Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        var error = new ErrorResponse("BUSINESS_RULE_VIOLATION", ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        var error = new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrationException(IntegrationException ex) {
        log.error("Falha na comunicação com o serviço de portfolio: {}",ex.getMessage());
        var error = new ErrorResponse("INTERNAL_ERROR", "Não foi possível processar sua solicitação no momento.", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage());
        var error = new ErrorResponse("INTERNAL_SERVER_ERROR", "Erro inesperado. Tente novamente mais tarde.", Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
