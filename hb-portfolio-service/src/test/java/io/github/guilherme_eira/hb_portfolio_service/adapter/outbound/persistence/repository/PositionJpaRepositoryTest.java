package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.InvestorEntity;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.PositionEntity;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.WalletEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PositionJpaRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("hb_portfolio")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private PositionJpaRepository positionRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private UUID walletId;
    private final String ticker = "WEGE3";

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        walletId = transactionTemplate.execute(status -> {
            var investor = new InvestorEntity();
            investor.setId(UUID.randomUUID());
            investor.setFullName("Guilherme Eira");
            investor.setEmail("gui@email.com");
            investor.setTaxId("74645828371");
            investor.setUsername("guieira");
            investor.setCreatedAt(Instant.now());
            entityManager.persist(investor);

            var wallet = new WalletEntity();
            wallet.setId(UUID.randomUUID());
            wallet.setOwner(investor);
            wallet.setAvailableBalance(BigDecimal.ZERO);
            wallet.setCreatedAt(Instant.now());
            entityManager.persist(wallet);

            var position = new PositionEntity();
            position.setId(UUID.randomUUID());
            position.setWalletId(wallet.getId());
            position.setTicker(ticker);
            position.setQuantity(100);
            position.setCreatedAt(Instant.now());
            entityManager.persist(position);

            return wallet.getId();
        });
    }

    @Test
    void shouldWaitWhenPessimisticLockIsApplied() throws Exception {
        long lockDurationMillis = 1000;

        CompletableFuture<Void> thread1 = CompletableFuture.runAsync(() -> {
            transactionTemplate.execute(status -> {
                positionRepository.findByWalletIdAndTickerWithLock(walletId, ticker);
                try {
                    Thread.sleep(lockDurationMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
        });

        Thread.sleep(200);

        long startTime = System.currentTimeMillis();

        CompletableFuture<Void> thread2 = CompletableFuture.runAsync(() -> {
            transactionTemplate.execute(status -> {
                positionRepository.findByWalletIdAndTickerWithLock(walletId, ticker);
                return null;
            });
        });

        CompletableFuture.allOf(thread1, thread2).get(5, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        assertTrue(executionTime >= (lockDurationMillis - 200),
                "A Thread 2 deveria ter sido bloqueada pelo Lock Pessimista. Tempo: " + executionTime + "ms");
    }
}