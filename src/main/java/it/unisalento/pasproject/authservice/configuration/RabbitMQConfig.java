package it.unisalento.pasproject.authservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    // ------  DATA  ------

    // Needed for data consistency
    @Value("${rabbitmq.queue.data.name}")
    private String dataQueue;

    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;

    @Value("${rabbitmq.routing.data.key}")
    private String dataRoutingKey;

    @Bean
    public Queue dataQueue() {
        return new Queue(dataQueue);
    }

    @Bean
    public TopicExchange dataExchange() {
        return new TopicExchange(dataExchange);
    }

    @Bean
    public Binding dataBinding() {
        return BindingBuilder
                .bind(dataQueue())
                .to(dataExchange())
                .with(dataRoutingKey);
    }

    // ------  END DATA  ------



    /**
     * Creates a message converter for JSON messages.
     *
     * @return a new Jackson2JsonMessageConverter instance.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates an AMQP template for sending messages.
     *
     * @param connectionFactory the connection factory to use.
     * @return a new RabbitTemplate instance.
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
