package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.CreateOrderCommand;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCreatedOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.ReserveResourcePayload;
import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.in.CreateOrderUseCase;
import io.github.guilherme_eira.hb_oms_service.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_oms_service.application.port.out.MarketDataRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.PortfolioServicePort;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderSide;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderType;
import io.github.guilherme_eira.hb_oms_service.domain.model.Order;
import io.github.guilherme_eira.hb_oms_service.domain.service.MarketSessionValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final MarketDataRepository marketDataRepository;
    private final PortfolioServicePort portfolioService;
    private final EventPublisherPort eventPublisher;
    private final MarketSessionValidator marketSession;

    @Override
    @Transactional
    public OrderCreatedOutput execute(CreateOrderCommand cmd) {
        marketSession.validate(LocalDateTime.now());

        var assetRule = marketDataRepository.getAssetRule(cmd.ticker())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado: " + cmd.ticker()));

        BigDecimal priceLimit = calculatePriceLimit(cmd, assetRule.referencePrice());

        if (cmd.type() == OrderType.LIMIT) {
            validateLimits(priceLimit, assetRule.referencePrice(), cmd.side());
            assetRule.validatePrice(priceLimit);
        }
        assetRule.validateQuantity(cmd.totalQuantity());

        BigDecimal amountToReserve = (cmd.side() == OrderSide.BID)
                ? priceLimit.multiply(BigDecimal.valueOf(cmd.totalQuantity()))
                : BigDecimal.valueOf(cmd.totalQuantity());

        var order = Order.create(cmd.investorId(), cmd.ticker(), cmd.totalQuantity(), priceLimit, cmd.type(), cmd.side());

        ReserveResourcePayload toBeReserved = new ReserveResourcePayload(
                order.getInvestorId(),
                order.getId(),
                order.getSide(),
                (cmd.side() == OrderSide.ASK) ? order.getTicker() : null,
                amountToReserve
        );

        portfolioService.reserveResource(toBeReserved);

        orderRepository.save(order);

        var orderOutput = new OrderCreatedOutput(
                order.getId(), order.getTicker(), order.getTotalQuantity(),
                order.getPriceLimit(), order.getType(), order.getSide(), order.getCreatedAt()
        );

        eventPublisher.publishOrderCreated(orderOutput);

        return orderOutput;
    }

    private BigDecimal calculatePriceLimit(CreateOrderCommand cmd, BigDecimal refPrice) {
        if (cmd.type() == OrderType.LIMIT) return cmd.priceLimit();

        return (cmd.side() == OrderSide.BID)
                ? refPrice.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP)
                : refPrice.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateLimits(BigDecimal priceLimit, BigDecimal assetPrice, OrderSide side) {
        BigDecimal maxVariation = new BigDecimal("0.20");
        BigDecimal upperLimit = assetPrice.multiply(BigDecimal.ONE.add(maxVariation));
        BigDecimal lowerLimit = assetPrice.multiply(BigDecimal.ONE.subtract(maxVariation));

        if (side == OrderSide.BID && priceLimit.compareTo(upperLimit) > 0) {
            throw new BusinessException("Preço de compra muito acima do mercado (limite: " + upperLimit + ")");
        }

        if (side == OrderSide.ASK && priceLimit.compareTo(lowerLimit) < 0) {
            throw new BusinessException("Preço de venda muito abaixo do mercado (limite: " + lowerLimit + ")");
        }

    }

}
