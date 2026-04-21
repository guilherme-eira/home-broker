package io.github.guilherme_eira.hb_oms_service.domain.model;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Order {
    private UUID id;
    private UUID investorId;
    private String ticker;
    private Integer totalQuantity;
    private BigDecimal priceLimit;
    private Integer filledQuantity;
    private BigDecimal averagePrice;
    private OrderType type;
    private OrderSide side;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Order(UUID id, UUID investorId, String ticker, Integer totalQuantity, BigDecimal priceLimit, Integer filledQuantity, BigDecimal averagePrice, OrderType type, OrderSide side, OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.investorId = investorId;
        this.ticker = ticker;
        this.totalQuantity = totalQuantity;
        this.priceLimit = priceLimit;
        this.filledQuantity = filledQuantity;
        this.averagePrice = averagePrice;
        this.type = type;
        this.side = side;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(UUID investorId, String ticker, Integer totalQuantity, BigDecimal priceLimit, OrderType type, OrderSide side){
        return new Order(
                UUID.randomUUID(),
                investorId,
                ticker,
                totalQuantity,
                priceLimit,
                0,
                BigDecimal.ZERO,
                type,
                side,
                OrderStatus.OPEN,
                Instant.now(),
                null
        );
    }

    public void update(OrderStatus status, Integer executionQuantity, BigDecimal averageExecutionPrice){
        this.status = status;
        this.filledQuantity = executionQuantity;
        this.averagePrice = averageExecutionPrice;
        this.updatedAt = Instant.now();
    }

    public Boolean isFinished(){
        return this.status == OrderStatus.CANCELLED || this.status == OrderStatus.EXPIRED || this.status == OrderStatus.FILLED;
    }

    public void markAsCancellationPending(){
        this.status = OrderStatus.CANCELLATION_PENDING;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getInvestorId() {
        return investorId;
    }

    public String getTicker() {
        return ticker;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getPriceLimit() {
        return priceLimit;
    }

    public Integer getFilledQuantity() {
        return filledQuantity;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public OrderType getType() {
        return type;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
