package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
@Data
public class TradeEntity {
    @Id
    @Column(nullable = false, unique = true)
    private UUID id;
    @Column(nullable = false)
    private UUID bidOrderId;
    @Column(nullable = false)
    private UUID askOrderId;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private Instant executedAt;
}
