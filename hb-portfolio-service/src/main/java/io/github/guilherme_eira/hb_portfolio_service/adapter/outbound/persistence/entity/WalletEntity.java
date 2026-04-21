package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data
public class WalletEntity {

    @Id
    private UUID id;
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private InvestorEntity owner;
    @Column(nullable = false)
    private BigDecimal availableBalance;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant updatedAt;
}
