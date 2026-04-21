package io.github.guilherme_eira.hb_portfolio_service.application.port.in;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.DepositCommand;

public interface DepositUseCase {
    void execute(DepositCommand cmd);
}
