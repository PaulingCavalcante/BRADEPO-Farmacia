package com.farmacia.dto;

import java.math.BigDecimal;

/**
 * Resumo financeiro da venda exposto na resposta: valores e desconto aplicado.
 * (Fase 5 acrescenta a comissão do vendedor quando a venda é no balcão.)
 */
public record ResumoFinanceiro(BigDecimal valorBruto, BigDecimal percentualDesconto,
                               BigDecimal valorDesconto, BigDecimal valorLiquido,
                               String descricaoDesconto) {
}
