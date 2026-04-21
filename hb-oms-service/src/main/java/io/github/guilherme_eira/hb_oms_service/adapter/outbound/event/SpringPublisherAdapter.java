package io.github.guilherme_eira.hb_oms_service.adapter.outbound.event;

import io.github.guilherme_eira.hb_oms_service.application.dto.output.ExecutionProcessedOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCanceledOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCreatedOutput;
import io.github.guilherme_eira.hb_oms_service.application.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishOrderCreated(OrderCreatedOutput output) {
        applicationEventPublisher.publishEvent(output);
    }

    @Override
    public void publishOrderCanceled(OrderCanceledOutput output) {
        applicationEventPublisher.publishEvent(output);
    }

    @Override
    public void publishExecutionProcessed(ExecutionProcessedOutput output) {
        applicationEventPublisher.publishEvent(output);
    }
}
