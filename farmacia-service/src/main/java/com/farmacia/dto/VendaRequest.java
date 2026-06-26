package com.farmacia.dto;

/**
 * Pedido de venda. {@code cpf} é opcional (exceto produto controlado).
 * {@code canal} aceita INTERNET (padrão se omitido) ou BALCAO; {@code vendedor}
 * é obrigatório quando a venda é no BALCAO (gera comissão).
 */
public record VendaRequest(String cpf, String produto, String canal, String vendedor) {
}
