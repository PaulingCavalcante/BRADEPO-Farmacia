package com.farmacia.componentes.desconto;

import java.math.BigDecimal;

/**
 * Entrada do cálculo de desconto.
 *
 * @param valorBruto        valor da compra antes do desconto
 * @param clienteCadastrado se o cliente está cadastrado (habilita desconto progressivo)
 * @param idoso             se o cliente é idoso
 * @param convenio          se o cliente possui convênio
 */
public record DescontoContexto(BigDecimal valorBruto, boolean clienteCadastrado,
                               boolean idoso, boolean convenio) {
}
