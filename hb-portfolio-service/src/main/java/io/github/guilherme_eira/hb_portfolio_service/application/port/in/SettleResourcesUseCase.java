package io.github.guilherme_eira.hb_portfolio_service.application.port.in;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.SettleResourcesCommand;

public interface SettleResourcesUseCase {
    void execute(SettleResourcesCommand cmd);
}
