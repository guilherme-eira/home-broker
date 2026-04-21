package io.github.guilherme_eira.hb_oms_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto.CreateOrderRequest;
import io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto.OrderCreatedResponse;
import io.github.guilherme_eira.hb_oms_service.adapter.inbound.http.dto.OrderResponse;
import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity.OrderEntity;
import io.github.guilherme_eira.hb_oms_service.application.dto.input.CreateOrderCommand;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderCreatedOutput;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.OrderOutput;
import io.github.guilherme_eira.hb_oms_service.domain.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderEntity toEntity(Order domain);
    Order toDomain(OrderEntity entity);
    @Mapping(source = "investorId", target = "investorId")
    CreateOrderCommand toCommand(UUID investorId, CreateOrderRequest req);
    OrderCreatedResponse toOrderCreatedResponse(OrderCreatedOutput output);
    OrderResponse toOrderResponse(OrderOutput output);
}
