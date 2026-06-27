package com.farmacia.rabbitmq.consumer;

import com.farmacia.rabbitmq.config.RabbitMQConfig;
import com.farmacia.rabbitmq.event.VendaAutorizadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EstoqueAlertaConsumer {

    private static final Logger log = LoggerFactory.getLogger(EstoqueAlertaConsumer.class);

    private static final int LIMIAR_ESTOQUE_BAIXO = 5;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTOQUE_ALERTA)
    public void verificarEstoque(VendaAutorizadaEvent event) {
        if (event.estoqueRestante() <= LIMIAR_ESTOQUE_BAIXO) {
            log.warn("[ESTOQUE] Nível baixo! produto={} restante={} — solicitar reposição ao fornecedor.",
                    event.produto(), event.estoqueRestante());
        }
    }
}
