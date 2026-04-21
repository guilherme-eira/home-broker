package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.GetOrdersQuery;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderOutput;
import io.github.guilherme_eira.hb_oms_service.application.port.in.GetOrdersUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOrdersService implements GetOrdersUseCase {

    private final OrderRepository repository;

    @Override
    public Page<OrderOutput> execute(GetOrdersQuery query, Pageable pageable) {
        var orders = repository.findByInvestorIdAndStatus(query.investorId(), query.status(), pageable);

        return orders.map(o -> {
            return new OrderOutput(
                    o.getId(),
                    o.getTicker(),
                    o.getTotalQuantity(),
                    o.getPriceLimit(),
                    o.getFilledQuantity(),
                    o.getAveragePrice(),
                    o.getType(),
                    o.getSide(),
                    o.getStatus(),
                    o.getCreatedAt()
            );
        });
    }
}
