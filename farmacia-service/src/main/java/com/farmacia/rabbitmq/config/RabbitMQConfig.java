package com.farmacia.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "farmacia.exchange";

    public static final String QUEUE_VENDA_AUTORIZADA = "farmacia.venda.autorizada";
    public static final String QUEUE_VENDA_NEGADA     = "farmacia.venda.negada";
    public static final String QUEUE_VENDA_CONTROLADO = "farmacia.venda.controlado";
    public static final String QUEUE_ESTOQUE_ALERTA   = "farmacia.estoque.alerta";

    public static final String RK_AUTORIZADA = "venda.autorizada";
    public static final String RK_NEGADA     = "venda.negada";
    public static final String RK_CONTROLADO = "venda.controlado";

    @Bean
    public TopicExchange farmaciaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // --- Filas ---

    @Bean public Queue queueVendaAutorizada() { return QueueBuilder.durable(QUEUE_VENDA_AUTORIZADA).build(); }
    @Bean public Queue queueVendaNegada()     { return QueueBuilder.durable(QUEUE_VENDA_NEGADA).build(); }
    @Bean public Queue queueVendaControlado() { return QueueBuilder.durable(QUEUE_VENDA_CONTROLADO).build(); }
    @Bean public Queue queueEstoqueAlerta()   { return QueueBuilder.durable(QUEUE_ESTOQUE_ALERTA).build(); }

    // --- Bindings ---
    // venda.autorizada → duas filas (pub/sub via topic exchange)

    @Bean
    public Binding bindingVendaAutorizada() {
        return BindingBuilder.bind(queueVendaAutorizada()).to(farmaciaExchange()).with(RK_AUTORIZADA);
    }

    @Bean
    public Binding bindingEstoqueAlerta() {
        return BindingBuilder.bind(queueEstoqueAlerta()).to(farmaciaExchange()).with(RK_AUTORIZADA);
    }

    @Bean
    public Binding bindingVendaNegada() {
        return BindingBuilder.bind(queueVendaNegada()).to(farmaciaExchange()).with(RK_NEGADA);
    }

    @Bean
    public Binding bindingVendaControlado() {
        return BindingBuilder.bind(queueVendaControlado()).to(farmaciaExchange()).with(RK_CONTROLADO);
    }

    // --- Serialização JSON ---

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
