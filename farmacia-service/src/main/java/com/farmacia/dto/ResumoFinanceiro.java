package com.farmacia.dto;

import java.math.BigDecimal;

/**
 * Resumo financeiro da venda exposto na resposta: valores, desconto aplicado e,
 * quando a venda é no balcão, a comissão do vendedor.
 */
public record ResumoFinanceiro(BigDecimal valorBruto, BigDecimal percentualDesconto,
                               BigDecimal valorDesconto, BigDecimal valorLiquido,
                               String descricaoDesconto, BigDecimal comissao) {
}
