package com.farmacia.dto;

import java.math.BigDecimal;

/** Dados de entrada para cadastrar/atualizar um produto. */
public record ProdutoRequest(String nome, String categoria, BigDecimal preco,
                             Integer estoque, Boolean controlado) {
}
