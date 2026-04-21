package io.github.guilherme_eira.hb_portfolio_service.domain.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
