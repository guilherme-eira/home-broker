package io.github.guilherme_eira.hb_portfolio_service.infra.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

    private final String USER_EVENTS_DLX = "portfolio.user-events.dlx";
    private final String ORDER_EVENTS_DLX = "oms.order-events.dlx";

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public DirectExchange userEventsExchange() {
        return new DirectExchange("auth.user-events.direct", true, false);
    }

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder
                .durable("portfolio.user-created.queue")
                .withArgument("x-dead-letter-exchange", USER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", "user.created.dead")
                .build();
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
                .bind(userCreatedQueue())
                .to(userEventsExchange())
                .with("user.created");
    }

    @Bean
    public DirectExchange userEventsDeadLetterExchange() {
        return new DirectExchange(USER_EVENTS_DLX);
    }

    @Bean
    public Queue userCreatedDeadLetterQueue() {
        return QueueBuilder.durable("portfolio.user-created.dlq").build();
    }

    @Bean
    public Binding userCreatedDeadLetterBinding() {
        return BindingBuilder.bind(userCreatedDeadLetterQueue()).to(userEventsDeadLetterExchange()).with("user.created.dead");
    }

    @Bean
    public DirectExchange orderEventsExchange() {
        return new DirectExchange("oms.order-events.direct", true, false);
    }

    @Bean
    public Queue orderExecutedQueue() {
        return QueueBuilder
                .durable("portfolio.order-executed.queue")
                .withArgument("x-dead-letter-exchange", ORDER_EVENTS_DLX)
                .withArgument("x-dead-letter-routing-key", "order.executed.dead")
                .build();
    }

    @Bean
    public Binding OrderExecutedBinding() {
        return BindingBuilder
                .bind(orderExecutedQueue())
                .to(orderEventsExchange())
                .with("order.executed");
    }

    @Bean
    public DirectExchange orderEventsDeadLetterExchange() {
        return new DirectExchange(ORDER_EVENTS_DLX);
    }

    @Bean
    public Queue orderExecutedDeadLetterQueue() {
        return QueueBuilder.durable("portfolio.order-executed.dlq").build();
    }

    @Bean
    public Binding orderExecutedDeadLetterBinding() {
        return BindingBuilder
                .bind(orderExecutedDeadLetterQueue())
                .to(orderEventsDeadLetterExchange())
                .with("order.executed.dead");
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