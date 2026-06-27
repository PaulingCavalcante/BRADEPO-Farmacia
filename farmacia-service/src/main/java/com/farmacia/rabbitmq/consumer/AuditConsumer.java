package com.farmacia.rabbitmq.consumer;

import com.farmacia.rabbitmq.config.RabbitMQConfig;
import com.farmacia.rabbitmq.event.VendaAutorizadaEvent;
import com.farmacia.rabbitmq.event.VendaNegadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VENDA_AUTORIZADA)
    public void onVendaAutorizada(VendaAutorizadaEvent event) {
        log.info("[AUDIT] AUTORIZADA id={} | produto={} cpf={} canal={} valorLiquido=R${}",
                event.vendaId(), event.produto(), event.cpf(), event.canal(), event.valorLiquido());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VENDA_NEGADA)
    public void onVendaNegada(VendaNegadaEvent event) {
        log.info("[AUDIT] NEGADA | produto={} cpf={} motivo={} dataHora={}",
                event.produto(), event.cpf(), event.motivo(), event.dataHora());
    }
}
