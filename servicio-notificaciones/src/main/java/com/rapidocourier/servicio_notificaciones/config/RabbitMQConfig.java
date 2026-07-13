package com.rapidocourier.servicio_notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NOTIFICACIONES = "notificaciones.queue";
    public static final String EXCHANGE_NOTIFICACIONES = "notificaciones.exchange";
    public static final String ROUTING_KEY_NOTIFICACIONES = "notificaciones.routing.key";

    public static final String DLQ_NOTIFICACIONES = "notificaciones.dlq.queue";
    public static final String DLX_NOTIFICACIONES = "notificaciones.dlq.exchange";
    public static final String DLQ_ROUTING_KEY = "notificaciones.dlq.routing.key";

    @Bean
    public Queue notificacionesDlqQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable(DLQ_NOTIFICACIONES).build();
    }

    @Bean
    public TopicExchange notificacionesDlqExchange() {
        return new TopicExchange(DLX_NOTIFICACIONES);
    }

    @Bean
    public Binding bindingNotificacionesDlq(Queue notificacionesDlqQueue, TopicExchange notificacionesDlqExchange) {
        return BindingBuilder.bind(notificacionesDlqQueue).to(notificacionesDlqExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue notificacionesQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable(QUEUE_NOTIFICACIONES)
                .withArgument("x-dead-letter-exchange", DLX_NOTIFICACIONES)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange notificacionesExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICACIONES);
    }

    @Bean
    public Binding bindingNotificaciones(Queue notificacionesQueue, TopicExchange notificacionesExchange) {
        return BindingBuilder.bind(notificacionesQueue).to(notificacionesExchange).with(ROUTING_KEY_NOTIFICACIONES);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.core.RabbitTemplate template = new org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory factory = new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
