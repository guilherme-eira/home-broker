package io.github.guilherme_eira.hb_oms_service.application.service;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.GetOrderByIdQuery;
import io.github.guilherme_eira.hb_oms_service.application.exception.ResourceNotFoundException;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.domain.model.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GetOrderByIdServiceTest {

    @Mock
    private OrderRepository repository;

    @InjectMocks
    private GetOrderByIdService service;

    @Test
    public void shouldThrowExceptionIfOrderDoesNotExits(){
        var orderId = UUID.randomUUID();
        var query = new GetOrderByIdQuery(UUID.randomUUID(), orderId);

        given(repository.findById(orderId)).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.execute(query));
    }

    @Test
    public void shouldThrowExceptionIfOrderDoesNotBelongToInvestor(){
        var orderId = UUID.randomUUID();
        var query = new GetOrderByIdQuery(UUID.randomUUID(), orderId);
        var orderMock = mock(Order.class);

        given(repository.findById(orderId)).willReturn(Optional.of(orderMock));
        given(orderMock.getInvestorId()).willReturn(UUID.randomUUID());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.execute(query));
    }

}