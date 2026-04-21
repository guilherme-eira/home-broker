package io.github.guilherme_eira.hb_oms_service.infra.bootstrap;

import io.github.guilherme_eira.hb_oms_service.application.port.out.AssetRuleRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.MarketDataRepository;
import io.github.guilherme_eira.hb_oms_service.domain.vo.AssetRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class AssetRuleCacheInitializer implements CommandLineRunner {

    private final AssetRuleRepository assetRuleRepository;
    private final MarketDataRepository marketDataRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando carregamento dos ativos...");

        if (marketDataRepository.isCachePopulated()) {
            log.info("Cache já populado. Pulando inicialização.");
            return;
        }

        List<AssetRule> assetRules = assetRuleRepository.findAll();
        marketDataRepository.initializeAssetRules(assetRules);

        log.info("{} ativos carregados na memória local.", assetRules.size());
    }
}
