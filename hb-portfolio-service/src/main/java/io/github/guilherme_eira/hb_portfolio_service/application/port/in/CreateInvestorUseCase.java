package io.github.guilherme_eira.hb_portfolio_service.application.port.in;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.CreateInvestorCommand;

public interface CreateInvestorUseCase {
    void execute(CreateInvestorCommand event);
}
