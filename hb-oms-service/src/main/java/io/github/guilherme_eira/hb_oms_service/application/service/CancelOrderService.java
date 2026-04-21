package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.CancelOrderCommand;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCanceledOutput;
import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.in.CancelOrderUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.service.MarketSessionValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepository repository;
    private final EventPublisherPort eventPublisher;
    private final MarketSessionValidator marketSession;

    @Override
    @Transactional
    public void execute(CancelOrderCommand cmd) {
        marketSession.validate(LocalDateTime.now());

        var order = repository.findByIdWithLock(cmd.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Ordem não encontrada."));

        if (!order.getInvestorId().equals(cmd.investorId())) {
            throw new ResourceNotFoundException("Ordem não encontrada.");
        }

        if (order.isFinished()){
            throw new BusinessException("Não é possível cancelar essa ordem pois ela já foi finalizada.");
        }

        if (order.getStatus() == OrderStatus.CANCELLATION_PENDING) {
            throw new BusinessException("Um pedido de cancelamento para esta ordem já está em processamento.");
        }

        order.markAsCancellationPending();
        repository.save(order);

        var output = new OrderCanceledOutput(order.getId());

        eventPublisher.publishOrderCanceled(output);
    }
}
