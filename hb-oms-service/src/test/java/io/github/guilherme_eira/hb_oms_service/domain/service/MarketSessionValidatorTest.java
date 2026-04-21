package io.github.guilherme_eira.hb_oms_service.domain.service;

import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MarketSessionValidatorTest {

    private final MarketSessionValidator validator = new MarketSessionValidator();

    @Test
    void shouldAllowDuringMarketHours() {
        LocalDateTime mondayMorning = LocalDateTime.of(2026, 4, 6, 14, 0);
        assertDoesNotThrow(() -> validator.validate(mondayMorning));
    }

    @ParameterizedTest
    @CsvSource({
            "2026-04-06T09:59:59",
            "2026-04-06T17:55:01",
            "2026-04-05T14:00:00",
            "2026-04-04T14:00:00"
    })
    void shouldThrowWhenOutsideHoursOrWeekend(String dateTime) {
        LocalDateTime input = LocalDateTime.parse(dateTime);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(input));

        assertTrue(ex.getMessage().contains("Negociação indisponível"));
    }
}