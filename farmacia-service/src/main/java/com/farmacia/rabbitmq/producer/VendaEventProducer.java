package com.farmacia.rabbitmq.producer;

import com.farmacia.rabbitmq.config.RabbitMQConfig;
import com.farmacia.rabbitmq.event.VendaAutorizadaEvent;
import com.farmacia.rabbitmq.event.VendaNegadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class VendaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(VendaEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public VendaEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarVendaAutorizada(VendaAutorizadaEvent event) {
        enviar(RabbitMQConfig.RK_AUTORIZADA, event);
        if (event.controlado()) {
            enviar(RabbitMQConfig.RK_CONTROLADO, event);
        }
    }

    public void publicarVendaNegada(VendaNegadaEvent event) {
        enviar(RabbitMQConfig.RK_NEGADA, event);
    }

    private void enviar(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
            log.info("[RABBITMQ] → {} | {}", routingKey, event);
        } catch (AmqpException e) {
            // Broker indisponível: loga e segue — a venda já foi processada e salva no banco.
            log.warn("[RABBITMQ] Broker indisponível, evento descartado ({}): {}", routingKey, e.getMessage());
        }
    }
}
