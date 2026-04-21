package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.projection;

import java.math.BigDecimal;

public record BlockedAssetProjection(
        String ticker,
        BigDecimal quantity
) {
}
