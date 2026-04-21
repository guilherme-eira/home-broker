package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity;

import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class OrderEntity {
    @Id
    @Column(nullable = false, unique = true)
    private UUID id;
    @Column(nullable = false)
    private UUID investorId;
    @Column(nullable = false)
    private String ticker;
    @Column(nullable = false)
    private Integer totalQuantity;
    @Column(nullable = false)
    private BigDecimal priceLimit;
    private Integer filledQuantity;
    private BigDecimal averagePrice;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType type;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderSide side;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant updatedAt;

}
