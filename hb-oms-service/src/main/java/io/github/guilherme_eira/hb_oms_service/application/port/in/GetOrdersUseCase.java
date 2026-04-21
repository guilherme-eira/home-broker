package io.github.guilherme_eira.hb_oms_service.application.port.in;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.GetOrdersQuery;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderOutput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetOrdersUseCase {
    Page<OrderOutput> execute(GetOrdersQuery query, Pageable pageable);
}
