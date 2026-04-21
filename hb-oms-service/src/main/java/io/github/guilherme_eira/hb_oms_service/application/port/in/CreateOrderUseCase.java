package io.github.guilherme_eira.hb_oms_service.application.port.in;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.CreateOrderCommand;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCreatedOutput;

public interface CreateOrderUseCase {
    OrderCreatedOutput execute(CreateOrderCommand cmd);
}
