package io.github.guilherme_eira.hb_oms_service.infra.config.amqp;

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

    private final String ORDER_EVENTS_DLX = "oms.order-events.dlx";

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
    public DirectExchange matchingEngineOrderEventsEx(){
        return new DirectExchange("matching-engine.order-events.direct");
    }

    @Bean
    public Queue orderExecutedQueue() {
        return QueueBuilder
                .durable("oms.order-executed.queue")
                .withArgument("x-dead-letter-exchange", ORDER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", "order.executed.dead")
                .build();
    }

    @Bean
    public Binding OrderExecutedBinding() {
        return BindingBuilder
                .bind(orderExecutedQueue())
                .to(matchingEngineOrderEventsEx())
                .with("order.executed");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(ORDER_EVENTS_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("oms.order-executed.dlq").build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("order.executed.dead");
    }

    @Bean
    public DirectExchange omsOrderEventsExchange() {
        return new DirectExchange("oms.order-events.direct", true, false);
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
