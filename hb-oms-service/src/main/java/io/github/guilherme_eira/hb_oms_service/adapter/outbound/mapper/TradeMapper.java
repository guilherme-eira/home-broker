package io.github.guilherme_eira.hb_oms_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity.TradeEntity;
import io.github.guilherme_eira.hb_oms_service.domain.model.Trade;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TradeMapper {
    TradeEntity toEntity(Trade domain);
    Trade toDomain(TradeEntity entity);
}
