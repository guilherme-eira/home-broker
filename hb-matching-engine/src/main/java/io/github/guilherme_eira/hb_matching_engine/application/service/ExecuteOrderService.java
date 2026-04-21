package io.github.guilherme_eira.hb_matching_engine.application.service;

import io.github.guilherme_eira.hb_matching_engine.application.dto.ExecuteOrderCommand;
import io.github.guilherme_eira.hb_matching_engine.application.helper.OrderExecutionDispatcher;
import io.github.guilherme_eira.hb_matching_engine.application.port.in.ExecuteOrderUseCase;
import io.github.guilherme_eira.hb_matching_engine.application.port.out.OrderBookRepository;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Order;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ExecuteOrderService implements ExecuteOrderUseCase {

    private final OrderBookRepository repository;
    private final OrderExecutionDispatcher dispatcher;

    @Override
    public void execute(ExecuteOrderCommand cmd) {
        var incomeOrder = Order.newOrder(
                cmd.orderId(),
                cmd.ticker(),
                cmd.totalQuantity(),
                cmd.priceLimit(),
                cmd.type(),
                cmd.side()
        );

        while (incomeOrder.getFilledQuantity() < incomeOrder.getTotalQuantity()) {
            String bestOfferId = repository.getBestOfferId(incomeOrder.getTicker(), incomeOrder.getSide());
            if (bestOfferId == null) {
                log.warn("Não há ofertas de {} para o ticker {}.", incomeOrder.getSide().opposite(), incomeOrder.getTicker());
                break;
            }

            UUID counterId = UUID.fromString(bestOfferId);
            var details = repository.getOrderDetails(counterId);

            if (details.isEmpty()) {
                log.warn("Os detalhes da ordem {} não foram encontrados.", counterId);
                repository.removeOrder(counterId, incomeOrder.getTicker(), incomeOrder.getSide().opposite());
                continue;
            }

            var counterPartyOrder = repository.mapToOrder(details);

            if (!incomeOrder.canMatchWith(counterPartyOrder.getPriceLimit())) {
                log.warn("Não foi possível realizar o match pois os limites não correspondem.");
                break;
            }

            executeTrade(incomeOrder, counterPartyOrder);

            if (Objects.equals(counterPartyOrder.getFilledQuantity(), counterPartyOrder.getTotalQuantity())) {
                dispatcher.dispatch(counterPartyOrder, OrderStatus.FILLED);
            } else {
                dispatcher.dispatch(counterPartyOrder, OrderStatus.PARTIAL);
                repository.saveToBook(counterPartyOrder);
            }

            if (Objects.equals(incomeOrder.getFilledQuantity(), incomeOrder.getTotalQuantity())) break;
        }

        if (incomeOrder.getFilledQuantity() < incomeOrder.getTotalQuantity()) {
            if (incomeOrder.getType() == OrderType.LIMIT) {
                if (incomeOrder.getFilledQuantity() != 0) {
                    dispatcher.dispatch(incomeOrder, OrderStatus.PARTIAL);
                }
                repository.saveToBook(incomeOrder);
            } else {
                dispatcher.dispatch(incomeOrder, OrderStatus.EXPIRED);
            }
        } else {
            dispatcher.dispatch(incomeOrder, OrderStatus.FILLED);
        }
    }

    private void executeTrade(Order incomeOrder, Order counterPartyOrder) {
        var incomeRemaining = incomeOrder.getTotalQuantity() - incomeOrder.getFilledQuantity();
        var counterPartyRemaining = counterPartyOrder.getTotalQuantity() - counterPartyOrder.getFilledQuantity();
        var tradeQuantity = Math.min(incomeRemaining, counterPartyRemaining);

        var newTrade = Trade.create(
                incomeOrder.getSide() == OrderSide.BID ? incomeOrder.getId() : counterPartyOrder.getId(),
                incomeOrder.getSide() == OrderSide.ASK ? incomeOrder.getId() : counterPartyOrder.getId(),
                counterPartyOrder.getPriceLimit(),
                tradeQuantity
        );

        incomeOrder.addToFilledQuantity(tradeQuantity);
        counterPartyOrder.addToFilledQuantity(tradeQuantity);

        incomeOrder.addTrade(newTrade);
        counterPartyOrder.addTrade(newTrade);
    }
}
