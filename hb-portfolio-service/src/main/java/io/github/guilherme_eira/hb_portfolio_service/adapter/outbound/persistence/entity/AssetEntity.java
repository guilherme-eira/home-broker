package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "assets")
@Data
public class AssetEntity {
    @Id
    private String ticker;
}
