package com.farmacia.dto;

import com.farmacia.componentes.sefaz.NotaFiscal;

/**
 * Resposta de uma venda. Em NEGADA, {@code nota}/protocolos/{@code financeiro}
 * vêm nulos e {@code motivo} explica a recusa. Em AUTORIZADA, traz o canal, o
 * vendedor (se balcão) e o {@code financeiro} com valores, desconto e comissão.
 */
public record VendaResponse(String status, NotaFiscal nota, String protocoloSefaz,
                            String protocoloAns, String motivo, String canal,
                            String vendedor, ResumoFinanceiro financeiro) {
}
