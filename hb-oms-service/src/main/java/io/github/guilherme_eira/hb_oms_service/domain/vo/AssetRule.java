package io.github.guilherme_eira.hb_oms_service.domain.vo;

import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import java.math.BigDecimal;

public record AssetRule(
        String ticker,
        BigDecimal minTick,
        Integer lotSize,
        BigDecimal referencePrice
) {
    public void validatePrice(BigDecimal price) {
        if (price == null || price.remainder(minTick).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Preço deve ser múltiplo de " + minTick);
        }
    }

    public void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0 || quantity % lotSize != 0) {
            throw new BusinessException("Quantidade deve ser múltiplo de " + lotSize);
        }
    }
}