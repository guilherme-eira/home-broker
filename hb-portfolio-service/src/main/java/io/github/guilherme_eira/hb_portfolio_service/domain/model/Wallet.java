package io.github.guilherme_eira.hb_portfolio_service.domain.model;

import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Wallet {
    private UUID id;
    private Investor owner;
    private BigDecimal availableBalance;
    private Instant createdAt;
    private Instant updatedAt;

    public Wallet(UUID id, Investor owner, BigDecimal availableBalance, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.owner = owner;
        this.availableBalance = availableBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Wallet create(Investor owner) {
        return new Wallet(
                UUID.randomUUID(),
                owner,
                BigDecimal.ZERO,
                Instant.now(),
                null
        );
    }

    public void reserve(BigDecimal amount) {
        if (amount.compareTo(availableBalance) > 0) {
            throw new BusinessException("Saldo insuficiente para reserva.");
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("Quantidade inválida.");
        }
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Investor getOwner() {
        return owner;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
