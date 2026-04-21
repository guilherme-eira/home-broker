package io.github.guilherme_eira.hb_portfolio_service.application.port.in;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.AddPositionCommand;

public interface AddPositionUseCase {
    void execute(AddPositionCommand cmd);
}
