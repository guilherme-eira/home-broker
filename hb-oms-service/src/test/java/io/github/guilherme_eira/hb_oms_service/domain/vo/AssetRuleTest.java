package io.github.guilherme_eira.hb_oms_service.domain.vo;

import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetRuleTest {

    private final AssetRule defaultRule = new AssetRule(
            "WEGE3",
            new BigDecimal("0.01"),
            100,
            new BigDecimal("35.00")
    );

    @ParameterizedTest
    @ValueSource(strings = {"10.00", "10.01", "0.01", "100.55"})
    void shouldAcceptPricesMatchingMinTick(String price) {
        assertDoesNotThrow(() -> defaultRule.validatePrice(new BigDecimal(price)));
    }

    @Test
    void shouldRejectPriceNotMatchingLargeTick() {
        var largeTickRule = new AssetRule("PETR4", new BigDecimal("0.05"), 100, new BigDecimal("30.00"));
        assertThrows(BusinessException.class, () -> largeTickRule.validatePrice(new BigDecimal("10.53")));
    }

    @Test
    void shouldThrowExceptionForNullPrice() {
        assertThrows(BusinessException.class, () -> defaultRule.validatePrice(null));
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 200, 1000})
    void shouldAcceptQuantitiesMatchingLotSize(int quantity) {
        assertDoesNotThrow(() -> defaultRule.validateQuantity(quantity));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10, 50, 150})
    void shouldRejectQuantitiesNotMatchingLotSize(int quantity) {
        assertThrows(BusinessException.class, () -> defaultRule.validateQuantity(quantity));
    }

    @Test
    void shouldAcceptAnyPositiveQuantityForFractionalLot() {
        var fractionalRule = new AssetRule("WEGE3F", new BigDecimal("0.01"), 1, new BigDecimal("35.00"));
        assertDoesNotThrow(() -> fractionalRule.validateQuantity(1));
        assertDoesNotThrow(() -> fractionalRule.validateQuantity(99));
    }

    @Test
    void shouldThrowExceptionForNullQuantity() {
        assertThrows(BusinessException.class, () -> defaultRule.validateQuantity(null));
    }
}