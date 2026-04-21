package io.github.guilherme_eira.hb_matching_engine.adapter.outbound.persistence;

import io.github.guilherme_eira.hb_matching_engine.application.port.out.OrderBookRepository;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_matching_engine.domain.enums.OrderType;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Order;
import io.github.guilherme_eira.hb_matching_engine.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Log4j2
@Repository
@RequiredArgsConstructor
public class OrderBookRepositoryAdapter implements OrderBookRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String BID_PREFIX = "book:bid:";
    private static final String ASK_PREFIX = "book:ask:";
    private static final String DETAIL_PREFIX = "order:detail:";

    public void saveToBook(Order order) {
        String zsetKey = (order.getSide() == OrderSide.BID ? BID_PREFIX : ASK_PREFIX) + order.getTicker();
        String detailKey = DETAIL_PREFIX + order.getId();

        try {
            String executionsJson = objectMapper.writeValueAsString(order.getTrades());

            var details = toOrderDetails(order, executionsJson);

            long OFFSET_2024 = 1704067200L;
            long secondsSinceOffset = order.getCreatedAt().getEpochSecond() - OFFSET_2024;
            double timeFactor = secondsSinceOffset / 10_000_000_000.0;

            double score = (order.getSide() == OrderSide.ASK)
                    ? order.getPriceLimit().doubleValue() + timeFactor
                    : order.getPriceLimit().doubleValue() + (0.0000001 - timeFactor);

            redisTemplate.opsForHash().putAll(detailKey, details);
            redisTemplate.opsForZSet().add(zsetKey, order.getId().toString(), score);

        } catch (JsonProcessingException e) {
            log.error("Erro ao processar ordem {}: {}", order.getId(), e.getMessage());
        }
    }

    private Map<String, String> toOrderDetails(Order order, String executionsJson) {
        Map<String, String> details = new HashMap<>();
        details.put("orderId", order.getId().toString());
        details.put("ticker", order.getTicker());
        details.put("totalQuantity", String.valueOf(order.getTotalQuantity()));
        details.put("priceLimit", order.getPriceLimit().toString());
        details.put("side", order.getSide().name());
        details.put("filledQuantity", String.valueOf(order.getFilledQuantity()));
        details.put("type", order.getType().name());
        details.put("createdAt", String.valueOf(order.getCreatedAt().getEpochSecond()));
        details.put("executions", executionsJson);
        return details;
    }

    public String getBestOfferId(String ticker, OrderSide side) {
        String zsetKey = (side == OrderSide.BID ? ASK_PREFIX : BID_PREFIX) + ticker;
        Set<String> result;

        if (side == OrderSide.BID) {
            result = redisTemplate.opsForZSet().range(zsetKey, 0, 0);
        } else {
            result = redisTemplate.opsForZSet().reverseRange(zsetKey, 0, 0);
        }

        return result != null && !result.isEmpty() ? result.iterator().next() : null;
    }

    public Map<String, String> getOrderDetails(UUID orderId) {
        return redisTemplate.<String, String>opsForHash().entries(DETAIL_PREFIX + orderId);
    }

    public Order mapToOrder(Map<String, String> details) {
        if (details == null || details.isEmpty()) return null;

        try {
            ObjectMapper mapper = objectMapper.findAndRegisterModules();

            List<Trade> executions = mapper.readValue(
                    details.get("executions"),
                    new TypeReference<List<Trade>>() {}
            );

            return Order.fromState(
                    UUID.fromString(details.get("orderId")),
                    details.get("ticker"),
                    Integer.parseInt(details.get("totalQuantity")),
                    new BigDecimal(details.get("priceLimit")),
                    Integer.parseInt(details.get("filledQuantity")),
                    OrderType.valueOf(details.get("type")),
                    OrderSide.valueOf(details.get("side")),
                    Instant.ofEpochSecond(Long.parseLong(details.get("createdAt"))),
                    executions
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao desserializar ordem do Redis", e);
        }
    }

    public void removeOrder(UUID orderId, String ticker, OrderSide side) {
        String zsetKey = (side == OrderSide.BID ? BID_PREFIX : ASK_PREFIX) + ticker;
        redisTemplate.opsForZSet().remove(zsetKey, orderId.toString());
        redisTemplate.delete(DETAIL_PREFIX + orderId.toString());
    }
}


