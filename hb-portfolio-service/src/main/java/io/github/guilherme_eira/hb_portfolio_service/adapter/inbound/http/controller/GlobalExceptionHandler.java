package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.controller;

import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.http.dto.ErrorResponse;
import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.time.Instant;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex) {
        log.error("Tentativa de acesso não autorizada: {}", ex.getMessage());
        var error = new ErrorResponse("UNAUTHORIZED", "Acesso negado. Por favor, faça login novamente.", Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        var error = new ErrorResponse(
                "FORBIDDEN",
                "Você não tem permissão para acessar este recurso.",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn("Falha de validação na requisição: {}", message);
        var error = new ErrorResponse("INVALID_INPUT", message, Instant.now());
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro inesperado: ", ex);
        var error = new ErrorResponse("INTERNAL_SERVER_ERROR", "Erro inesperado. Tente novamente mais tarde.", Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
