package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.output.ExecutionProcessedOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.input.ProcessExecutionCommand;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.in.ProcessExecutionUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_oms_service.application.port.out.MarketDataRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.TradeRepository;
import io.github.guilherme_eira.hb_oms_service.domain.model.Trade;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProcessExecutionService implements ProcessExecutionUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final MarketDataRepository marketDataRepository;
    private final EventPublisherPort eventPublisher;

    @Override
    @Transactional
    public void execute(ProcessExecutionCommand cmd) {
        var order = orderRepository.findByIdWithLock(cmd.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Ordem não encontrada: " + cmd.orderId()));

        if (cmd.trades() != null && !cmd.trades().isEmpty()) {

            boolean hasNewTrades = false;

            for (var t : cmd.trades()) {
                if (!tradeRepository.existsById(t.tradeId())) {
                    var newTrade = Trade.create(
                            t.tradeId(), t.bidId(), t.askId(),
                            t.quantity(), t.price(), t.executedAt()
                    );
                    tradeRepository.save(newTrade);
                    hasNewTrades = true;
                }
            }

            if (hasNewTrades) {
                var lastTrade = cmd.trades().getLast();
                marketDataRepository.updatePrice(order.getTicker(), lastTrade.price());
            }
        }

        order.update(cmd.status(), cmd.filledQuantity(), cmd.averagePrice());
        orderRepository.save(order);

        var output = new ExecutionProcessedOutput(
                order.getId(),
                order.getTicker(),
                order.getStatus(),
                order.getFilledQuantity(),
                order.getAveragePrice()
        );

        eventPublisher.publishExecutionProcessed(output);
    }
}
