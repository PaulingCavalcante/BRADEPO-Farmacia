package com.farmacia.rabbitmq.event;

import com.farmacia.model.Canal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VendaAutorizadaEvent(
        Long vendaId,
        String cpf,
        String produto,
        boolean controlado,
        Canal canal,
        String vendedor,
        String notaId,
        String protocoloSefaz,
        String protocoloAns,
        BigDecimal valorLiquido,
        BigDecimal comissao,
        int estoqueRestante,
        LocalDateTime dataHora
) {}
