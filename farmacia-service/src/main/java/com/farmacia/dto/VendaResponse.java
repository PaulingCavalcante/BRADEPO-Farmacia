package com.farmacia.dto;

import com.farmacia.componentes.sefaz.NotaFiscal;

/**
 * Resposta de uma venda. Em NEGADA, {@code nota}/protocolos/{@code financeiro}
 * vêm nulos e {@code motivo} explica a recusa. Em AUTORIZADA, {@code financeiro}
 * traz valores e desconto.
 */
public record VendaResponse(String status, NotaFiscal nota, String protocoloSefaz,
                            String protocoloAns, String motivo, ResumoFinanceiro financeiro) {
}
