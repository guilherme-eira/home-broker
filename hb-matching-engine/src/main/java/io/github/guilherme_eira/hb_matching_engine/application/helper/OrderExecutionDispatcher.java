package io.github.guilherme_eira.hb_matching_engine.application.helper;

import io.github.guilherme_eira.hb_matching_engine.application.dto.OrderExecutedOutput;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.OrderBookRepository;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderExecutionDispatcher {

    private final OrderBookRepository repository;
    private final EventPublisherPort eventPublisher;

    public void dispatch(Order order, OrderStatus status) {
        var trades = order.getTrades().stream().map(t ->
                new OrderExecutedOutput.TradeOutputDTO(
                        t.getTradeId(),
                        t.getBidOrderId(),
                        t.getAskOrderId(),
                        t.getQuantity(),
                        t.getPrice(),
                        t.getExecutedAt()
                )
        ).toList();

        var output = new OrderExecutedOutput(
                order.getId(),
                status,
                order.getFilledQuantity(),
                order.calculateAveragePrice(),
                trades
        );

        if (status == OrderStatus.FILLED || status == OrderStatus.EXPIRED || status == OrderStatus.CANCELLED) {
            repository.removeOrder(order.getId(), order.getTicker(), order.getSide());
        }

        eventPublisher.publishOrderProcessed(output);
    }
}
