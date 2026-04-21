package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.ResourceReservationEntity;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.ResourceReservation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceReservationMapper {
    ResourceReservationEntity toEntity(ResourceReservation domain);
    ResourceReservation toDomain(ResourceReservationEntity entity);
}
