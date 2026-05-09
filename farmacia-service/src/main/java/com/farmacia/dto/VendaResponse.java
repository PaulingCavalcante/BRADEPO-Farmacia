package com.farmacia.dto;

import com.farmacia.componentes.sefaz.NotaFiscal;

public record VendaResponse(String status, NotaFiscal nota, String protocoloSefaz, String protocoloAns, String motivo) {
}
