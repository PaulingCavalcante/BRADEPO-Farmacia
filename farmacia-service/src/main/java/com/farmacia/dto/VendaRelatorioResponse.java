package com.farmacia.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha do relatório "vendas por período". */
public record VendaRelatorioResponse(Long id, String produto, String cpf, String canal,
                                     String vendedor, BigDecimal valorLiquido,
                                     BigDecimal comissao, LocalDateTime dataHora) {
}
