package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.CancelOrderCommand;
import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.model.Order;
import io.github.guilherme_eira.hb_oms_service.domain.service.MarketSessionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private EventPublisherPort eventPublisher;

    @Mock
    private MarketSessionValidator marketSession;

    @InjectMocks
    private CancelOrderService service;

    @Test
    void shouldCancelOrderSuccessfully() {
        var investorId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var cmd = new CancelOrderCommand(investorId, orderId);
        var order = mock(Order.class);

        given(order.getId()).willReturn(orderId);
        given(order.getInvestorId()).willReturn(investorId);
        given(order.isFinished()).willReturn(false);
        given(order.getStatus()).willReturn(OrderStatus.OPEN);
        given(repository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        service.execute(cmd);

        verify(marketSession).validate(any(LocalDateTime.class));
        verify(order).markAsCancellationPending();
        verify(repository).save(order);
        verify(eventPublisher).publishOrderCanceled(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        var orderId = UUID.randomUUID();
        var cmd = new CancelOrderCommand(UUID.randomUUID(), orderId);
        given(repository.findByIdWithLock(orderId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(cmd));
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishOrderCanceled(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderBelongsToAnotherInvestor() {
        var orderId = UUID.randomUUID();
        var cmd = new CancelOrderCommand(UUID.randomUUID(), orderId);
        var order = mock(Order.class);

        given(order.getInvestorId()).willReturn(UUID.randomUUID());
        given(repository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        assertThrows(ResourceNotFoundException.class, () -> service.execute(cmd));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishOrderCanceled(any());
    }

    @Test
    void throwExceptionWhenOrderIsAlreadyFinished() {
        var investorId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var cmd = new CancelOrderCommand(investorId, orderId);
        var order = mock(Order.class);

        given(repository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
        given(order.getInvestorId()).willReturn(investorId);
        given(order.isFinished()).willReturn(true);

        assertThrows(BusinessException.class, () -> service.execute(cmd));
        verify(repository, never()).save(any());
    }

    @Test
    void throwExceptionWhenCancellationIsAlreadyPending() {
        var investorId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var cmd = new CancelOrderCommand(investorId, orderId);
        var order = mock(Order.class);

        given(repository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
        given(order.getInvestorId()).willReturn(investorId);
        given(order.isFinished()).willReturn(false);
        given(order.getStatus()).willReturn(OrderStatus.CANCELLATION_PENDING);

        assertThrows(BusinessException.class, () -> service.execute(cmd));
    }
}