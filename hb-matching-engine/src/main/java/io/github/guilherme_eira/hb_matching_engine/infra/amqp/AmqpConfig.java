package io.github.guilherme_eira.hb_matching_engine.infra.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

    private final String ORDER_EVENTS_DLX = "matching-engine.order-events.dlx";

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange omsOrderEventsExchange() {
        return new DirectExchange("oms.order-events.direct", true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder
                .durable("matching-engine.order-created.queue")
                .withArgument("x-dead-letter-exchange", ORDER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", "order.created.dead")
                .build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(omsOrderEventsExchange())
                .with("order.created");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(ORDER_EVENTS_DLX);
    }

    @Bean
    public Queue orderCreatedDlq() {
        return QueueBuilder.durable("matching-engine.order-created.dlq").build();
    }

    @Bean
    public Binding orderCreatedDlqBinding() {
        return BindingBuilder.bind(orderCreatedDlq()).to(deadLetterExchange()).with("order.created.dead");
    }

    @Bean
    public Queue orderCanceledQueue() {
        return QueueBuilder
                .durable("matching-engine.order-canceled.queue")
                .withArgument("x-dead-letter-exchange", ORDER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", "order.canceled.dead")
                .build();
    }

    @Bean
    public Binding orderCanceledBinding() {
        return BindingBuilder
                .bind(orderCanceledQueue())
                .to(omsOrderEventsExchange())
                .with("order.canceled");
    }

    @Bean
    public Queue orderCanceledDlq() {
        return QueueBuilder.durable("matching-engine.order-canceled.dlq").build();
    }

    @Bean
    public Binding orderCanceledDlqBinding() {
        return BindingBuilder.bind(orderCanceledDlq()).to(deadLetterExchange()).with("order.canceled.dead");
    }

    @Bean
    public DirectExchange matchingEngineOrderEventsEx(){
        return new DirectExchange("matching-engine.order-events.direct");
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> initializeAdmin(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }
}
