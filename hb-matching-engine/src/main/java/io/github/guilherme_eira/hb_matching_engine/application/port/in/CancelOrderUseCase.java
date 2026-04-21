package io.github.guilherme_eira.hb_matching_engine.application.port.in;

import io.github.guilherme_eira.hb_matching_engine.application.dto.CancelOrderCommand;

public interface CancelOrderUseCase {
    void execute(CancelOrderCommand cmd);
}
