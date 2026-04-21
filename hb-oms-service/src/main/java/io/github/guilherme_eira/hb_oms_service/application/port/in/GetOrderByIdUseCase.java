package io.github.guilherme_eira.hb_oms_service.application.port.in;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.GetOrderByIdQuery;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderOutput;

public interface GetOrderByIdUseCase {
    OrderOutput execute(GetOrderByIdQuery query);
}
