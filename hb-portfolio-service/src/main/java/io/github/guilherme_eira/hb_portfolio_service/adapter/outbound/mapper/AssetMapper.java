package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.AssetEntity;
import io.github.guilherme_eira.hb_portfolio_service.domain.vo.Asset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    Asset toDomain(AssetEntity entity);
}
