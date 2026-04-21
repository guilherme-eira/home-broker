package io.github.guilherme_eira.hb_oms_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Trade {
    private UUID id;
    private UUID bidOrderId;
    private UUID askOrderId;
    private Integer quantity;
    private BigDecimal price;
    private Instant executedAt;

    public static Trade create(UUID id, UUID bidOrderId, UUID askOrderId, Integer quantity, BigDecimal price, Instant executedAt){
        return new Trade(
                id,
                bidOrderId,
                askOrderId,
                quantity,
                price,
                executedAt
        );
    }

    public Trade(UUID id, UUID bidOrderId, UUID askOrderId, Integer quantity, BigDecimal price, Instant executedAt) {
        this.id = id;
        this.bidOrderId = bidOrderId;
        this.askOrderId = askOrderId;
        this.quantity = quantity;
        this.price = price;
        this.executedAt = executedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBidOrderId() {
        return bidOrderId;
    }

    public UUID getAskOrderId() {
        return askOrderId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
