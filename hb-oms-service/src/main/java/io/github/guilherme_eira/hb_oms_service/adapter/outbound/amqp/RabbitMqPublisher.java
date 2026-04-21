package io.github.guilherme_eira.hb_oms_service.adapter.outbound.amqp;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.amqp.dto.ExecutionProcessedEvent;
import io.github.guilherme_eira.hb_oms_service.adapter.outbound.amqp.dto.OrderCreatedEvent;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.ExecutionProcessedOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCanceledOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCreatedOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Log4j2
@Component
@RequiredArgsConstructor
public class RabbitMqPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "oms.order-events.direct";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedOutput output) {
        var event = new OrderCreatedEvent(
                output.orderId(), output.ticker(), output.totalQuantity(),
                output.priceLimit(), output.type(), output.side(), output.createdAt()
        );
        rabbitTemplate.convertAndSend(EXCHANGE, "order.created", event);
        log.info("Evento de criação enviado p/ RabbitMQ | OrderId: {}", output.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCanceled(OrderCanceledOutput output) {
        var event = new OrderCanceledOutput(output.orderId());
        rabbitTemplate.convertAndSend(EXCHANGE, "order.canceled", event);
        log.info("Evento de cancelamento enviado p/ RabbitMQ | OrderId: {}", output.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleExecutionProcessed(ExecutionProcessedOutput output) {
        var event = new ExecutionProcessedEvent(
                output.orderId(), output.ticker(), output.status(),
                output.filledQuantity(), output.averagePrice()
        );
        rabbitTemplate.convertAndSend(EXCHANGE, "order.executed", event);
        log.info("Evento de execução enviado p/ RabbitMQ | OrderId: {}", output.orderId());
    }
}