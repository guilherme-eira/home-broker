package io.github.guilherme_eira.hb_oms_service.domain.service;

import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MarketSessionValidator {

    private static final LocalTime OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(17, 55);

    public void validate(LocalDateTime referenceTime) {
        DayOfWeek day = referenceTime.getDayOfWeek();
        LocalTime time = referenceTime.toLocalTime();

        boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
        boolean isOutsideHours = time.isBefore(OPEN_TIME) || time.isAfter(CLOSE_TIME);

        if (isWeekend || isOutsideHours) {
            throw new BusinessException("Negociação indisponível: fora do horário de pregão.");
        }
    }
}