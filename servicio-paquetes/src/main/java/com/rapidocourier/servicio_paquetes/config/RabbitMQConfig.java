package com.rapidocourier.servicio_paquetes.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NOTIFICACIONES = "notificaciones.exchange";
    public static final String EXCHANGE_PAQUETES = "paquetes.exchange";

    @Bean
    public TopicExchange notificacionesExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICACIONES);
    }

    @Bean
    public TopicExchange paquetesExchange() {
        return new TopicExchange(EXCHANGE_PAQUETES);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
