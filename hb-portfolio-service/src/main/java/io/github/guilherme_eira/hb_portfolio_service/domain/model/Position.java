package io.github.guilherme_eira.hb_portfolio_service.domain.model;

import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Position {
    private UUID id;
    private UUID walletId;
    private String ticker;
    private Integer quantity;
    private Instant createdAt;
    private Instant updatedAt;

    public Position(UUID id, UUID walletId, String ticker, Integer quantity, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.walletId = walletId;
        this.ticker = ticker;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Position create(UUID walletId, String ticker){
        return new Position(UUID.randomUUID(), walletId, ticker, 0, Instant.now(), null);
    }

    public void reserve(Integer quantityToLock) {
        if (this.quantity < quantityToLock) {
            throw new BusinessException("Quantidade de ações insuficiente para venda.");
        }
        this.quantity -= quantityToLock;
        this.updatedAt = Instant.now();
    }

    public void credit(Integer quantity){
        if (quantity <= 0){
            throw new BusinessException("Quantidade inválida.");
        }
        this.quantity += quantity;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public String getTicker() {
        return ticker;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
