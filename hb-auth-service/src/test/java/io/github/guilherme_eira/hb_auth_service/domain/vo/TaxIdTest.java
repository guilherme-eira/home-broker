package io.github.guilherme_eira.hb_auth_service.domain.vo;

import io.github.guilherme_eira.hb_auth_service.domain.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TaxIdTest {

    @Test
    void shouldAcceptValidCpf() {
        var taxId = new TaxId("746.458.283-71");
        Assertions.assertEquals("74645828371", taxId.getValue());
    }

    @Test
    void shouldThrowWhenDigitsAreRepeated() {
        Assertions.assertThrows(BusinessException.class, () -> new TaxId("11111111111"));
    }

    @Test
    void shouldThrowWhenLengthIsInvalid() {
        Assertions.assertThrows(BusinessException.class, () -> new TaxId("12345"));
    }
}