package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "investors")
@Data
public class InvestorEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true)
    private String taxId;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private Instant createdAt;
}
