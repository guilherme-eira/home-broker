package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.amqp;

import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.amqp.dto.ExecutionProcessedEvent;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.SettleResourcesCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.SettleResourcesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class OmsListener {

    private final SettleResourcesUseCase settleResourcesUseCase;

    @RabbitListener(queues = "portfolio.order-executed.queue")
    public void onOrderExecuted(ExecutionProcessedEvent event){
        var cmd = new SettleResourcesCommand(
                event.orderId(),
                event.ticker(),
                event.status(),
                event.filledQuantity(),
                event.averagePrice()
        );

        log.info("Evento de execução recebido | Iniciando liquidação financeira/custódia | OrderId: {} | Status: {} | FilledQty: {}",
                event.orderId(), event.status(), event.filledQuantity());

        settleResourcesUseCase.execute(cmd);
    }
}
