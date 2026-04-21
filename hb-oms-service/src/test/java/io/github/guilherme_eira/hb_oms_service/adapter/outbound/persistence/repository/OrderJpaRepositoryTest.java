package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.repository;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity.OrderEntity;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderJpaRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("hb_oms_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private OrderJpaRepository orderRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void shouldWaitWhenPessimisticLockIsAppliedToOrder() throws Exception {
        UUID orderId = transactionTemplate.execute(status -> {
            var order = createOrderEntity(UUID.randomUUID(), OrderStatus.OPEN);
            entityManager.persist(order);
            return order.getId();
        });

        long lockDurationMillis = 1000;

        CompletableFuture<Void> thread1 = CompletableFuture.runAsync(() -> {
            transactionTemplate.execute(status -> {
                orderRepository.findByIdWithLock(orderId);
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
                orderRepository.findByIdWithLock(orderId);
                return null;
            });
        });

        CompletableFuture.allOf(thread1, thread2).get(5, TimeUnit.SECONDS);
        long executionTime = System.currentTimeMillis() - startTime;

        assertTrue(executionTime >= (lockDurationMillis - 200));
    }

    @Test
    void shouldFilterOrdersByStatusUsingCustomQuery() {
        UUID investorId = UUID.randomUUID();

        transactionTemplate.execute(t -> {
            entityManager.persist(createOrderEntity(investorId, OrderStatus.OPEN));
            entityManager.persist(createOrderEntity(investorId, OrderStatus.FILLED));
            entityManager.persist(createOrderEntity(investorId, OrderStatus.CANCELLED));
            return null;
        });

        var pageable = PageRequest.of(0, 10);

        var allOrders = orderRepository.findByInvestorIdAndOrderStatus(investorId, null, pageable);
        var openOrders = orderRepository.findByInvestorIdAndOrderStatus(investorId, OrderStatus.OPEN, pageable);

        assertEquals(3, allOrders.getTotalElements());
        assertEquals(1, openOrders.getTotalElements());
        assertEquals(OrderStatus.OPEN, openOrders.getContent().get(0).getStatus());
    }

    @Test
    void shouldReturnEmptyPageWhenInvestorHasNoOrders() {
        var result = orderRepository.findByInvestorIdAndOrderStatus(UUID.randomUUID(), null, PageRequest.of(0, 10));
        assertEquals(0, result.getTotalElements());
    }

    private OrderEntity createOrderEntity(UUID investorId, OrderStatus status) {
        var entity = new OrderEntity();
        entity.setId(UUID.randomUUID());
        entity.setInvestorId(investorId);
        entity.setTicker("WEGE3");
        entity.setTotalQuantity(100);
        entity.setPriceLimit(new BigDecimal("35.00"));
        entity.setFilledQuantity(0);
        entity.setAveragePrice(BigDecimal.ZERO);
        entity.setStatus(status);
        entity.setType(OrderType.LIMIT);
        entity.setSide(OrderSide.BID);
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}