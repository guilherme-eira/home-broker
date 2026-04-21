package io.github.guilherme_eira.hb_matching_engine.application.port.in;

import io.github.guilherme_eira.hb_matching_engine.application.dto.ExecuteOrderCommand;

public interface ExecuteOrderUseCase {
    void execute(ExecuteOrderCommand cmd);
}
