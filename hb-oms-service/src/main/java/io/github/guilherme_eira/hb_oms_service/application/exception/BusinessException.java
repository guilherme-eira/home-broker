package io.github.guilherme_eira.hb_oms_service.application.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
