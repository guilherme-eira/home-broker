package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity;

import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationStatus;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resource_reservations")
@Data
public class ResourceReservationEntity {

    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID orderId;
    @Column(nullable = false)
    private UUID walletId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationType type;
    private String ticker;
    @Column(nullable = false)
    private BigDecimal totalVolume;
    @Column(nullable = false)
    private BigDecimal settledVolume;
    @Column(nullable = false)
    private BigDecimal remainingVolume;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant updatedAt;
}
