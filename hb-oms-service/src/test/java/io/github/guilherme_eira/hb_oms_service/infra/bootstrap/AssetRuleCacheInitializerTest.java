package io.github.guilherme_eira.hb_oms_service.infra.bootstrap;

import io.github.guilherme_eira.hb_oms_service.application.port.out.MarketDataRepository;
import io.github.guilherme_eira.hb_oms_service.domain.vo.AssetRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AssetRuleCacheInitializerTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private MarketDataRepository marketDataRepository;

    @Autowired
    private AssetRuleCacheInitializer initializer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
    }

    @Test
    void shouldPopulateRedisFromFlywayDataOnStartup() throws Exception {
        initializer.run();
        var result = marketDataRepository.getAssetRule("PETR4");
        assertTrue(result.isPresent(), "O ticker do Flyway deveria ter sido migrado para o Redis");
    }

    @Test
    void shouldSkipMigrationWhenRedisIsAlreadyPopulated() throws Exception {
        var manualRule = new AssetRule("VALE3", new BigDecimal("0.01"), 100, new BigDecimal("90.00"));
        marketDataRepository.initializeAssetRules(java.util.List.of(manualRule));

        initializer.run();

        var resultFromFlyway = marketDataRepository.getAssetRule("PETR4");
        assertTrue(resultFromFlyway.isEmpty(), "Não deveria ter carregado dados do Flyway se o cache já estava pronto");
    }
}