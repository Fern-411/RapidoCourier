package com.rapidocourier.servicio_auth.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue sagaRollbackQueue() {
        return new Queue("saga.auth.rollback.queue", true);
    }
    
    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange("saga.exchange");
    }
    
    @Bean
    public Binding bindingSagaRollback(Queue sagaRollbackQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(sagaRollbackQueue).to(sagaExchange).with("saga.auth.rollback.key");
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
