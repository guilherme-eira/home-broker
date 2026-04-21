package io.github.guilherme_eira.hb_portfolio_service.application.port.in;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.ReserveResourcesCommand;

public interface ReserveResourcesUseCase {
    void execute(ReserveResourcesCommand cmd);
}
