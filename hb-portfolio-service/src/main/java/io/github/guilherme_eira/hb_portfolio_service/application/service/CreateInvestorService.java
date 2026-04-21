package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.CreateInvestorCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.CreateInvestorUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.InvestorRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Investor;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateInvestorService implements CreateInvestorUseCase {

    private final InvestorRepository investorRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void execute(CreateInvestorCommand cmd) {

        var id = UUID.fromString(cmd.userId());

        if (investorRepository.existsById(id)){
            log.warn("Investidor de id {} já existe", id);
            return;
        }

        var investor = Investor.create(
                id,
                cmd.fullName(),
                cmd.email(),
                cmd.taxId(),
                cmd.username(),
                cmd.createdAt()
        );

        var created = investorRepository.save(investor);

        var wallet = Wallet.create(created);
        walletRepository.save(wallet);
    }
}
