package io.github.guilherme_eira.hb_matching_engine.adapter.outbound.persistence;

import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class OrderBookRepositoryAdapterTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private OrderBookRepositoryAdapter repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanRedis() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
    }

    @Test
    void shouldSaveAndGetOrderDetails() {
        var order = Order.newOrder(UUID.randomUUID(), "PETR4", 100, new BigDecimal("30.50"), OrderType.LIMIT, OrderSide.BID);

        repository.saveToBook(order);

        var details = repository.getOrderDetails(order.getId());
        assertFalse(details.isEmpty());
        assertEquals("PETR4", details.get("ticker"));
        assertEquals("30.50", details.get("priceLimit"));

        var mappedOrder = repository.mapToOrder(details);
        assertEquals(order.getId(), mappedOrder.getId());
        assertEquals(0, order.getPriceLimit().compareTo(mappedOrder.getPriceLimit()));
    }

    @Test
    void askPriorityPriceTest() {
        var cheaperId = UUID.randomUUID();
        var expensiveId = UUID.randomUUID();

        var cheapAsk = Order.newOrder(cheaperId, "VALE3", 100, new BigDecimal("80.00"), OrderType.LIMIT, OrderSide.ASK);
        var expensiveAsk = Order.newOrder(expensiveId, "VALE3", 100, new BigDecimal("81.00"), OrderType.LIMIT, OrderSide.ASK);

        repository.saveToBook(expensiveAsk);
        repository.saveToBook(cheapAsk);

        String bestOffer = repository.getBestOfferId("VALE3", OrderSide.BID);
        assertEquals(cheaperId.toString(), bestOffer);
    }

    @Test
    void bidPriorityPriceTest() {
        var lowerId = UUID.randomUUID();
        var higherId = UUID.randomUUID();

        var lowBid = Order.newOrder(lowerId, "WEGE3", 100, new BigDecimal("34.00"), OrderType.LIMIT, OrderSide.BID);
        var highBid = Order.newOrder(higherId, "WEGE3", 100, new BigDecimal("35.00"), OrderType.LIMIT, OrderSide.BID);

        repository.saveToBook(lowBid);
        repository.saveToBook(highBid);

        String bestOffer = repository.getBestOfferId("WEGE3", OrderSide.ASK);
        assertEquals(higherId.toString(), bestOffer);
    }

    @Test
    void timePriorityTest() {
        var firstId = UUID.randomUUID();
        var secondId = UUID.randomUUID();
        var ticker = "ITUB4";
        var price = new BigDecimal("25.00");

        var firstOrder = Order.newOrder(firstId, ticker, 100, price, OrderType.LIMIT, OrderSide.ASK);

        var secondOrder = Order.fromState(secondId, ticker, 100, price, 0, OrderType.LIMIT, OrderSide.ASK,
                Instant.now().plusSeconds(60), new java.util.ArrayList<>());

        repository.saveToBook(secondOrder);
        repository.saveToBook(firstOrder);

        String bestOffer = repository.getBestOfferId(ticker, OrderSide.BID);
        assertEquals(firstId.toString(), bestOffer, "A ordem criada primeiro deve ter prioridade no mesmo preço");
    }

    @Test
    void shouldRemoveOrderCompletely() {
        var id = UUID.randomUUID();
        var order = Order.newOrder(id, "PETR4", 100, new BigDecimal("30.00"), OrderType.LIMIT, OrderSide.BID);

        repository.saveToBook(order);
        repository.removeOrder(id, "PETR4", OrderSide.BID);

        assertNull(repository.getBestOfferId("PETR4", OrderSide.ASK));
        assertTrue(repository.getOrderDetails(id).isEmpty());
    }
}