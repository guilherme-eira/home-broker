package io.github.guilherme_eira.hb_auth_service.domain.vo;

import io.github.guilherme_eira.hb_auth_service.domain.exception.BusinessException;

import java.util.Objects;

public class TaxId {
    private final String value;

    public TaxId(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("CPF/CNPJ não pode ser nulo ou vazio");
        }

        String sanitized = value.replaceAll("\\D", "");

        if (sanitized.length() != 11 && sanitized.length() != 14){
            throw new BusinessException("Tamanho inválido");
        }

        if (sanitized.matches("(\\d)\\1*")) {
            throw new BusinessException("CPF/CNPJ não deve apresentar somente dígitos repetidos");
        }

        if (!isMathematicallyValid(sanitized)) {
            throw new BusinessException("CPF/CNPJ não cumpre a validação matemática");
        }

        this.value = sanitized;
    }

    public String getValue() {
        return value;
    }

    private boolean isMathematicallyValid(String s) {
        if (s.length() == 11) {
            return validateCpf(s);
        }
        return validateCnpj(s);
    }

    private boolean validateCpf(String cpf) {
        int digit1 = calculateDigit(cpf.substring(0, 9), 10);
        int digit2 = calculateDigit(cpf.substring(0, 9) + digit1, 11);
        return cpf.equals(cpf.substring(0, 9) + digit1 + digit2);
    }

    private int calculateDigit(String str, int weight) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            int digit = Character.getNumericValue(str.charAt(i));
            sum += digit * weight;
            weight--;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private boolean validateCnpj(String cnpj) {
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int digit1 = calculateCnpjDigit(cnpj.substring(0, 12), weights1);
        int digit2 = calculateCnpjDigit(cnpj.substring(0, 12) + digit1, weights2);

        return cnpj.equals(cnpj.substring(0, 12) + digit1 + digit2);
    }

    private int calculateCnpjDigit(String str, int[] weights) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            int digit = Character.getNumericValue(str.charAt(i));
            sum += digit * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxId taxId = (TaxId) o;
        return Objects.equals(value, taxId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
