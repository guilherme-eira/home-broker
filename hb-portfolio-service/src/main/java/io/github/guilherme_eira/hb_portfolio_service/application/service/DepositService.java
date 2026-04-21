package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.DepositCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.DepositUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositService implements DepositUseCase {

    private final WalletRepository repository;

    @Override
    @Transactional
    public void execute(DepositCommand cmd) {
        var wallet = repository.findByOwnerIdWithLock(cmd.investorId())
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma carteira encontrada."));
        wallet.credit(cmd.amount());
        repository.save(wallet);
    }
}
