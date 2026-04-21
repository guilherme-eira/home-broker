package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "asset_rules")
@Data
public class AssetRuleEntity {
    @Id
    @Column(nullable = false, unique = true)
    private String ticker;
    @Column(nullable = false)
    private BigDecimal minTick;
    @Column(nullable = false)
    private Integer lotSize;
    @Column(nullable = false)
    private BigDecimal referencePrice;
}
