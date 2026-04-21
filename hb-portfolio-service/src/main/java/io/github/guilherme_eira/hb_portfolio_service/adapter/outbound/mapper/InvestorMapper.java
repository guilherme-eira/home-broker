package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.InvestorEntity;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Investor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvestorMapper {
    InvestorEntity toEntity(Investor domain);
    Investor toDomain(InvestorEntity entity);
}
