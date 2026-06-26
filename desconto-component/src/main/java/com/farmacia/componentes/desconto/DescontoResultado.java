package com.farmacia.componentes.desconto;

import java.math.BigDecimal;

/**
 * Resultado do cálculo de desconto.
 *
 * @param percentual    percentual aplicado (ex.: 8.00 = 8%)
 * @param valorDesconto valor abatido
 * @param valorLiquido  valor a pagar depois do desconto
 * @param descricao     explicação de qual regra venceu (para exibir/auditar)
 */
public record DescontoResultado(BigDecimal percentual, BigDecimal valorDesconto,
                                BigDecimal valorLiquido, String descricao) {
}
