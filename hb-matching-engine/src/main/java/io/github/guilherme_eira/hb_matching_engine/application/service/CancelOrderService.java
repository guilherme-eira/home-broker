package io.github.guilherme_eira.hb_matching_engine.application.service;

import io.github.guilherme_eira.hb_matching_engine.application.dto.CancelOrderCommand;
import io.github.guilherme_eira.hb_matching_engine.application.helper.OrderExecutionDispatcher;
import io.github.guilherme_eira.hb_matching_engine.application.port.in.CancelOrderUseCase;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.OrderBookRepository;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderBookRepository repository;
    private final OrderExecutionDispatcher dispatcher;

    @Override
    public void execute(CancelOrderCommand cmd) {
        var details = repository.getOrderDetails(cmd.orderId());
        if (details.isEmpty()) {
            log.warn("Ordem {} não encontrada para cancelamento.", cmd.orderId());
            return;
        }
        var order = repository.mapToOrder(details);
        dispatcher.dispatch(order,OrderStatus.CANCELLED);
    }
}
