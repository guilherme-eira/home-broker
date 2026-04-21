package io.github.guilherme_eira.hb_portfolio_service.application.service;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.AddPositionCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.AddPositionUseCase;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.AssetRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.PositionRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.WalletRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Position;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddPositionService implements AddPositionUseCase {

    private final WalletRepository walletRepository;
    private final PositionRepository positionRepository;
    private final AssetRepository assetRepository;

    @Override
    @Transactional
    public void execute(AddPositionCommand cmd) {

        var assetOptional = assetRepository.findByTicker(cmd.ticker());

        if (assetOptional.isEmpty()) throw new BusinessException("Ativo não encontrado");

        var asset = assetOptional.get();

        var wallet = walletRepository.findByOwnerId(cmd.investorId())
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma carteira encontrada."));
        var position = positionRepository.findByWalletIdAndTickerWithLock(wallet.getId(), asset.ticker())
                .orElseGet(() -> positionRepository.save(Position.create(wallet.getId(), asset.ticker())));
        position.credit(cmd.quantity());
        positionRepository.save(position);
    }
}
