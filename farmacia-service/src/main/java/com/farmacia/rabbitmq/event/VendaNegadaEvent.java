package com.farmacia.rabbitmq.event;

import com.farmacia.model.Canal;

import java.time.LocalDateTime;

public record VendaNegadaEvent(
        String cpf,
        String produto,
        Canal canal,
        String motivo,
        LocalDateTime dataHora
) {}
