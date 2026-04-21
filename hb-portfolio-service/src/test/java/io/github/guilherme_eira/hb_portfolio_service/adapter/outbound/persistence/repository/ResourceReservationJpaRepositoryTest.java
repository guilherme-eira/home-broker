package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.InvestorEntity;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.ResourceReservationEntity;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.WalletEntity;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationStatus;
import io.github.guilherme_eira.hb_portfolio_service.domain.enums.ReservationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ResourceReservationJpaRepositoryTest {

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ResourceReservationJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private WalletEntity wallet;

    @BeforeEach
    void setUp() {
        var investor = new InvestorEntity();
        investor.setId(UUID.randomUUID());
        investor.setFullName("Guilherme Eira");
        investor.setEmail("gui@email.com");
        investor.setTaxId("74645828371");
        investor.setUsername("guieira");
        investor.setCreatedAt(Instant.now());
        entityManager.persist(investor);

        this.wallet = new WalletEntity();
        this.wallet.setId(UUID.randomUUID());
        this.wallet.setOwner(investor);
        this.wallet.setAvailableBalance(BigDecimal.ZERO);
        this.wallet.setCreatedAt(Instant.now());

        entityManager.persist(this.wallet);
    }

    @Test
    void shouldReturnZeroWhenNoBlockedBalance() {
        BigDecimal blocked = repository.getBlockedBalance(wallet.getId());
        assertThat(blocked).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldSumOnlyPendingBalanceReservations() {
        createReservation(BigDecimal.valueOf(100.50), ReservationType.BALANCE, ReservationStatus.PENDING, null);
        createReservation(BigDecimal.valueOf(50.00), ReservationType.BALANCE, ReservationStatus.PENDING, null);

        createReservation(BigDecimal.valueOf(1000.00), ReservationType.BALANCE, ReservationStatus.COMPLETED, null);
        createReservation(BigDecimal.valueOf(10.00), ReservationType.ASSET, ReservationStatus.PENDING, "WEGE3");

        BigDecimal blocked = repository.getBlockedBalance(wallet.getId());

        assertThat(blocked).isEqualByComparingTo(new BigDecimal("150.50"));
    }

    @Test
    void shouldGroupBlockedAssetsByTicker() {
        createReservation(BigDecimal.valueOf(10), ReservationType.ASSET, ReservationStatus.PENDING, "PETR4");
        createReservation(BigDecimal.valueOf(5), ReservationType.ASSET, ReservationStatus.PENDING, "PETR4");
        createReservation(BigDecimal.valueOf(20), ReservationType.ASSET, ReservationStatus.PENDING, "VALE3");

        createReservation(BigDecimal.valueOf(100), ReservationType.BALANCE, ReservationStatus.PENDING, null);

        var assets = repository.findAllBlockedAssets(walletId());

        assertEquals(2, assets.size());

        var petr4 = assets.stream().filter(a -> a.ticker().equals("PETR4")).findFirst().get();
        assertThat(petr4.quantity()).isEqualByComparingTo(new BigDecimal("15"));

        var vale3 = assets.stream().filter(a -> a.ticker().equals("VALE3")).findFirst().get();
        assertThat(vale3.quantity()).isEqualByComparingTo(new BigDecimal("20"));
    }

    private void createReservation(BigDecimal amount, ReservationType type, ReservationStatus status, String ticker) {
        var res = new ResourceReservationEntity();
        res.setId(UUID.randomUUID());
        res.setOrderId(UUID.randomUUID());
        res.setWalletId(wallet.getId());
        res.setType(type);
        res.setStatus(status);
        res.setRemainingVolume(amount);
        res.setSettledVolume(BigDecimal.ZERO);
        res.setTotalVolume(amount);
        res.setTicker(ticker);
        res.setCreatedAt(Instant.now());
        entityManager.persist(res);
    }

    private UUID walletId() {
        return wallet.getId();
    }
}