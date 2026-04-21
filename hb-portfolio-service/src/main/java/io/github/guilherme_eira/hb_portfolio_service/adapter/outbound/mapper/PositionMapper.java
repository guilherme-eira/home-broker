package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.PositionEntity;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Position;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PositionMapper {
    Position toDomain(PositionEntity entity);
    PositionEntity toEntity(Position domain);
}
