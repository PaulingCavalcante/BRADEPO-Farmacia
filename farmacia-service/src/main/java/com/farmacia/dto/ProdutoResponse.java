package com.farmacia.dto;

import java.math.BigDecimal;

/** Representação de um produto retornada pela API. */
public record ProdutoResponse(Long id, String nome, String categoria, BigDecimal preco,
                              int estoque, boolean controlado) {
}
