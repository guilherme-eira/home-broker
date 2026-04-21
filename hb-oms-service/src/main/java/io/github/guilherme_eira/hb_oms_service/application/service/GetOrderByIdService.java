package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.GetOrderByIdQuery;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderOutput;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.in.GetOrderByIdUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetOrderByIdService implements GetOrderByIdUseCase {

    private final OrderRepository repository;

    @Override
    public OrderOutput execute(GetOrderByIdQuery query) {
        var order = repository.findById(query.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Ordem não encontrada"));

        if (!(query.investorId().equals(order.getInvestorId()))){
            throw new ResourceNotFoundException("Ordem não encontrada");
        }

        return new OrderOutput(
                order.getId(),
                order.getTicker(),
                order.getTotalQuantity(),
                order.getPriceLimit(),
                order.getFilledQuantity(),
                order.getAveragePrice(),
                order.getType(),
                order.getSide(),
                order.getStatus(),
                order.getCreatedAt()
        );

    }
}
