package com.farmacia.componentes.cpf;

import org.springframework.stereotype.Component;

@Component
public class CpfValidatorImpl implements CpfValidator {

    @Override
    public boolean validar(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }
        String limpo = cpf.replaceAll("[^0-9]", "");
        if (limpo.length() != 11) {
            return false;
        }
        if (limpo.matches("(\\d)\\1{10}")) {
            return false;
        }
        int d1 = calcularDigito(limpo, 10);
        int d2 = calcularDigito(limpo, 11);
        return d1 == Character.getNumericValue(limpo.charAt(9))
            && d2 == Character.getNumericValue(limpo.charAt(10));
    }

    private int calcularDigito(String cpf, int peso) {
        int soma = 0;
        for (int i = 0; i < peso - 1; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (peso - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
