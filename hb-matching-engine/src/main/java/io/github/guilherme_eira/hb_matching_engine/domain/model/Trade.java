package io.github.guilherme_eira.hb_matching_engine.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Trade{
    private UUID tradeId;
    private UUID bidOrderId;
    private UUID askOrderId;
    private Integer quantity;
    private BigDecimal price;
    private Instant executedAt;

    public Trade() {
    }

    public Trade(UUID tradeId, UUID bidOrderId, UUID askOrderId, BigDecimal price, Integer quantity, Instant executedAt) {
        this.tradeId = tradeId;
        this.bidOrderId = bidOrderId;
        this.askOrderId = askOrderId;
        this.price = price;
        this.quantity = quantity;
        this.executedAt = executedAt;
    }

    public static Trade create(UUID bidOrderId, UUID askOrderId, BigDecimal price, Integer quantity){
        return new Trade(
                UUID.randomUUID(),
                bidOrderId,
                askOrderId,
                price,
                quantity,
                Instant.now()
        );
    }

    public UUID getTradeId() {
        return tradeId;
    }

    public UUID getBidOrderId() {
        return bidOrderId;
    }

    public UUID getAskOrderId() {
        return askOrderId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
