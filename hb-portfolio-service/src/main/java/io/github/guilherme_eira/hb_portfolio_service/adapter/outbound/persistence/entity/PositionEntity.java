package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "positions")
@Data
public class PositionEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID walletId;
    @Column(nullable = false)
    private String ticker;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant updatedAt;
}
