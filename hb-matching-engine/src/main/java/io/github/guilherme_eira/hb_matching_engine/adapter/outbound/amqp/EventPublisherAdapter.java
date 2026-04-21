package io.github.guilherme_eira.hb_matching_engine.adapter.outbound.amqp;

import io.github.guilherme_eira.hb_matching_engine.adapter.outbound.amqp.dto.OrderExecutedEvent;
import io.github.guilherme_eira.hb_matching_engine.application.dto.OrderExecutedOutput;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class EventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishOrderProcessed(OrderExecutedOutput output) {
        var event = new OrderExecutedEvent(
                output.orderId(),
                output.status(),
                output.filledQuantity(),
                output.averagePrice(),
                output.trades().stream()
                        .map(this::mapToTradeEvent)
                        .toList()
        );

        rabbitTemplate.convertAndSend("matching-engine.order-events.direct", "order.executed", event);
        log.info("Evento de execução enviado | OrderId: {} | Status: {} | FilledQty: {} | AvgPrice: {} | Trades: {}",
                output.orderId(),
                output.status(),
                output.filledQuantity(),
                output.averagePrice(),
                output.trades().size());
    }

    private OrderExecutedEvent.TradeResponseDTO mapToTradeEvent(OrderExecutedOutput.TradeOutputDTO t) {
        return new OrderExecutedEvent.TradeResponseDTO(
                t.marketTradeId(),
                t.bidOrderId(),
                t.askOrderId(),
                t.quantity(),
                t.price(),
                t.executedAt()
        );
    }
}
