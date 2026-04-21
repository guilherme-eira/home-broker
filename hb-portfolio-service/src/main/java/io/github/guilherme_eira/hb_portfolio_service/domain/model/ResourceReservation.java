package io.github.guilherme_eira.hb_portfolio_service.domain.model;

import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationStatus;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;
import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ResourceReservation {
    private UUID id;
    private UUID orderId;
    private UUID walletId;
    private ReservationType type;
    private String ticker;
    private BigDecimal totalVolume;
    private BigDecimal settledVolume;
    private BigDecimal remainingVolume;
    private ReservationStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public ResourceReservation(UUID id, UUID orderId, UUID walletId, ReservationType type, String ticker, BigDecimal totalVolume, BigDecimal settledVolume, BigDecimal remainingVolume, ReservationStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.walletId = walletId;
        this.type = type;
        this.ticker = ticker;
        this.totalVolume = totalVolume;
        this.settledVolume = settledVolume;
        this.remainingVolume = remainingVolume;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ResourceReservation forBalance(UUID orderId, UUID walletId, BigDecimal amount) {
        return new ResourceReservation(
                UUID.randomUUID(),
                orderId,
                walletId,
                ReservationType.BALANCE,
                null,
                amount,
                BigDecimal.ZERO,
                amount,
                ReservationStatus.PENDING,
                Instant.now(),
                null
        );
    }

    public static ResourceReservation forAsset(UUID orderId, UUID walletId, String ticker, BigDecimal quantity) {
        return new ResourceReservation(
                UUID.randomUUID(),
                orderId,
                walletId,
                ReservationType.ASSET,
                ticker,
                quantity,
                BigDecimal.ZERO,
                quantity,
                ReservationStatus.PENDING,
                Instant.now(),
                null
        );
    }

    public void updateVolume(Integer filledQuantity, BigDecimal averagePrice) {
        if (this.status != ReservationStatus.PENDING){
            throw new BusinessException("Não é possível concluir uma reserva que não está pendente.");
        };

        BigDecimal currentSettled;
        if (this.type == ReservationType.BALANCE) {
            currentSettled = averagePrice.multiply(BigDecimal.valueOf(filledQuantity));
        } else {
            currentSettled = BigDecimal.valueOf(filledQuantity);
        }

        this.remainingVolume = this.totalVolume.subtract(currentSettled).max(BigDecimal.ZERO);
        this.settledVolume = currentSettled;
        this.updatedAt = Instant.now();
    }

    public void complete(Wallet wallet) {
        if (this.status != ReservationStatus.PENDING){
            throw new BusinessException("Não é possível concluir uma reserva que não está pendente.");
        }

        if (this.type == ReservationType.ASSET) {
            throw new BusinessException("Para finalizar reservas de ativos, é necessário informar a Position.");
        }

        if (this.remainingVolume.compareTo(BigDecimal.ZERO) > 0) {
            wallet.credit(this.remainingVolume);
        }

        this.status = ReservationStatus.COMPLETED;
        this.remainingVolume = BigDecimal.ZERO;
        this.updatedAt = Instant.now();
    }

    public void complete(Position position) {
        if (this.status != ReservationStatus.PENDING){
            throw new BusinessException("Não é possível concluir uma reserva que não está pendente.");
        }

        if (this.remainingVolume.compareTo(BigDecimal.ZERO) > 0 && position != null) {
            position.credit(this.remainingVolume.intValue());
        }

        this.status = ReservationStatus.COMPLETED;
        this.remainingVolume = BigDecimal.ZERO;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public ReservationType getType() {
        return type;
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public BigDecimal getSettledVolume() {
        return settledVolume;
    }

    public BigDecimal getRemainingVolume() {
        return remainingVolume;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
