package io.github.guilherme_eira.hb_matching_engine.domain.model;

import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final String ticker;
    private final Integer totalQuantity;
    private final BigDecimal priceLimit;
    private Integer filledQuantity;
    private final OrderType type;
    private final OrderSide side;
    private Instant createdAt;
    private final List<Trade> trades;

    public Order(UUID id, String ticker, Integer totalQuantity, BigDecimal priceLimit, Integer filledQuantity, OrderType type, OrderSide side, Instant createdAt, List<Trade> trades) {
        this.id = id;
        this.ticker = ticker;
        this.totalQuantity = totalQuantity;
        this.priceLimit = priceLimit;
        this.filledQuantity = filledQuantity;
        this.type = type;
        this.side = side;
        this.createdAt = createdAt;
        this.trades = trades;
    }

    public static Order newOrder(UUID id, String ticker, Integer totalQuantity, BigDecimal priceLimit, OrderType type, OrderSide side) {
        return new Order(id, ticker, totalQuantity, priceLimit, 0, type, side, Instant.now(), new ArrayList<>());
    }

    public static Order fromState(UUID id, String ticker, Integer totalQuantity, BigDecimal priceLimit,
                                  Integer filledQuantity, OrderType type, OrderSide side, Instant createdAt,
                                  List<Trade> trades) {
        return new Order(id, ticker, totalQuantity, priceLimit, filledQuantity, type, side, createdAt, trades);
    }

    public void addTrade(Trade trade){
        this.trades.add(trade);
    }

    public void addToFilledQuantity(Integer quantity){
        this.filledQuantity = this.filledQuantity + quantity;
    }

    public boolean canMatchWith(BigDecimal counterPartyPrice) {
        if (side == OrderSide.BID) {
            return priceLimit.compareTo(counterPartyPrice) >= 0;
        } else {
            return priceLimit.compareTo(counterPartyPrice) <= 0;
        }
    }

    public BigDecimal calculateAveragePrice() {
        if (trades.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalVolume = trades.stream()
                .map(t -> t.getPrice().multiply(new BigDecimal(t.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalVolume.divide(new BigDecimal(filledQuantity), 8, RoundingMode.HALF_UP);
    }

    public UUID getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPriceLimit() {
        return priceLimit;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public Integer getFilledQuantity() {
        return filledQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Trade> getTrades() {
        return trades;
    }
}
