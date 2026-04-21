package io.github.guilherme_eira.hb_oms_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.entity.AssetRuleEntity;
import io.github.guilherme_eira.hb_oms_service.domain.vo.AssetRule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssetRuleMapper {
    AssetRule toDomain(AssetRuleEntity entity);
}
