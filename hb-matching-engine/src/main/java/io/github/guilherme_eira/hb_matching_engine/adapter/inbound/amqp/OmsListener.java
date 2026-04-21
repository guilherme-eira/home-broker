package io.github.guilherme_eira.hb_matching_engine.adapter.inbound.amqp;

import io.github.guilherme_eira.hb_matching_engine.adapter.inbound.amqp.dto.OrderCanceledEvent;
import io.github.guilherme_eira.hb_matching_engine.adapter.inbound.amqp.dto.OrderCreatedEvent;
import io.github.guilherme_eira.hb_matching_engine.application.dto.CancelOrderCommand;
import io.github.guilherme_eira.hb_matching_engine.application.dto.ExecuteOrderCommand;
import io.github.guilherme_eira.hb_matching_engine.application.port.in.CancelOrderUseCase;
import io.github.guilherme_eira.hb_matching_engine.application.port.in.ExecuteOrderUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class OmsListener {

    private final ExecuteOrderUseCase executeOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    @RabbitListener(queues = "matching-engine.order-created.queue", concurrency = "1")
    public void onOrderCreated(OrderCreatedEvent event){
        var cmd = new ExecuteOrderCommand(
                event.orderId(),
                event.ticker(),
                event.totalQuantity(),
                event.priceLimit(),
                event.type(),
                event.side(),
                event.createdAt()
        );

        log.info("Evento de criação recebido | Processando execução | OrderId: {} | Ticker: {} | Qty: {}",
                event.orderId(), event.ticker(), event.totalQuantity());

        executeOrderUseCase.execute(cmd);
    }

    @RabbitListener(queues = "matching-engine.order-canceled.queue", concurrency = "1")
    public void onOrderCanceled(OrderCanceledEvent event){
        var cmd = new CancelOrderCommand(event.orderId());

        log.info("Evento de cancelamento recebido | Processando remoção do livro | OrderId: {}",
                event.orderId());

        cancelOrderUseCase.execute(cmd);
    }
}
