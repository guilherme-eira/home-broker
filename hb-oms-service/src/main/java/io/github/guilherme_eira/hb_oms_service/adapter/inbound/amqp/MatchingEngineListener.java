package io.github.guilherme_eira.hb_oms_service.adapter.inbound.amqp;

import io.github.guilherme_eira.hb_oms_service.adapter.inbound.amqp.dto.OrderExecutedEvent;
import io.github.guilherme_eira.hb_oms_service.application.dto.input.ProcessExecutionCommand;
import io.github.guilherme_eira.hb_oms_service.application.port.in.ProcessExecutionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MatchingEngineListener {

    private final ProcessExecutionUseCase processExecutionUseCase;

    @RabbitListener(queues = "oms.order-executed.queue")
    public void onOrderExecuted(OrderExecutedEvent event) {
        var cmd = new ProcessExecutionCommand(
                event.orderId(),
                event.status(),
                event.filledQuantity(),
                event.averagePrice(),
                event.trades().stream()
                        .map(t -> new ProcessExecutionCommand.TradeCommandDTO(
                                t.tradeId(), t.bidOrderId(), t.askOrderId(),
                                t.quantity(), t.price(), t.executedAt()
                        )).toList()
        );

        log.info("Evento de execução recebido | OrderId: {} | Status: {} | FilledQty: {}",
                event.orderId(), event.status(), event.filledQuantity());

        processExecutionUseCase.execute(cmd);
    }

}
