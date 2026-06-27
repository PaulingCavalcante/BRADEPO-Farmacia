package com.farmacia.rabbitmq.consumer;

import com.farmacia.rabbitmq.config.RabbitMQConfig;
import com.farmacia.rabbitmq.event.VendaAutorizadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ControladoAlertaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ControladoAlertaConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VENDA_CONTROLADO)
    public void onVendaControlado(VendaAutorizadaEvent event) {
        log.warn("[VIGILÂNCIA] Medicamento controlado dispensado | produto={} cpf={} protocoloAns={} vendaId={}",
                event.produto(), event.cpf(), event.protocoloAns(), event.vendaId());
        // Ponto de extensão: notificar ANVISA, disparar e-mail para farmacêutico responsável, etc.
    }
}
