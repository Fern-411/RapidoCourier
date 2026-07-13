package com.rapidocourier.servicio_envios.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PAQUETES = "paquetes.exchange";
    public static final String QUEUE_ENVIOS_NUEVO_PAQUETE = "envios.nuevo_paquete.queue";
    public static final String ROUTING_KEY_PAQUETE_CREADO = "paquetes.creado";

    @Bean
    public TopicExchange paquetesExchange() {
        return new TopicExchange(EXCHANGE_PAQUETES);
    }

    @Bean
    public Queue enviosNuevoPaqueteQueue() {
        return new Queue(QUEUE_ENVIOS_NUEVO_PAQUETE, true);
    }

    @Bean
    public Binding bindingEnviosNuevoPaquete(Queue enviosNuevoPaqueteQueue, TopicExchange paquetesExchange) {
        return BindingBuilder.bind(enviosNuevoPaqueteQueue).to(paquetesExchange).with(ROUTING_KEY_PAQUETE_CREADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
