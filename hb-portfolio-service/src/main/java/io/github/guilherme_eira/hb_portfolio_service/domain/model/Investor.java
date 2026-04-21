package io.github.guilherme_eira.hb_portfolio_service.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Investor {
    private UUID id;
    private String fullName;
    private String email;
    private String taxId;
    private String username;
    private Instant createdAt;

    public Investor(UUID id, String fullName, String email, String taxId, String username, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.taxId = taxId;
        this.username = username;
        this.createdAt = createdAt;
    }

    public static Investor create(UUID id, String fullName, String email, String taxId, String username, Instant createdAt){
        return new Investor(
                id,
                fullName,
                email,
                taxId,
                username,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
