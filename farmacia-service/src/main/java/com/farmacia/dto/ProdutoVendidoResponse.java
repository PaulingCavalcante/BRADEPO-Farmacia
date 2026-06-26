package com.farmacia.dto;

import java.math.BigDecimal;

/** Linha do relatório "produtos mais vendidos". */
public record ProdutoVendidoResponse(String produto, Long quantidade, BigDecimal totalLiquido) {
}
