package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_oms_service.application.port.out.MarketDataRepository;
import io.github.guilherme_eira.hb_oms_service.domain.vo.AssetRule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MarketDataRepositoryAdapter implements MarketDataRepository {

    private final StringRedisTemplate redisTemplate;
    private static final String RULE_PREFIX = "asset:rule:";

    @Override
    public boolean isCachePopulated() {
        Set<String> keys = redisTemplate.keys(RULE_PREFIX + "*");
        return keys != null && !keys.isEmpty();
    }

    @Override
    public void initializeAssetRules(List<AssetRule> assetRules) {
        assetRules.forEach(this::saveRule);
    }

    @Override
    public Optional<AssetRule> getAssetRule(String ticker) {
        Map<Object, Object> data = redisTemplate.opsForHash().entries(RULE_PREFIX + ticker);

        if (data.isEmpty()) return Optional.empty();

        return Optional.of(new AssetRule(
                (String) data.get("ticker"),
                new BigDecimal((String) data.get("minTick")),
                Integer.parseInt((String) data.get("lotSize")),
                new BigDecimal((String) data.get("referencePrice"))
        ));
    }

    @Override
    public void updatePrice(String ticker, BigDecimal price) {
        redisTemplate.opsForHash().put(RULE_PREFIX + ticker, "referencePrice", price.toString());
    }

    private void saveRule(AssetRule rule) {
        Map<String, String> map = new HashMap<>();
        map.put("ticker", rule.ticker());
        map.put("minTick", rule.minTick().toString());
        map.put("lotSize", rule.lotSize().toString());
        map.put("referencePrice", rule.referencePrice().toString());

        redisTemplate.opsForHash().putAll(RULE_PREFIX + rule.ticker(), map);
    }
}
