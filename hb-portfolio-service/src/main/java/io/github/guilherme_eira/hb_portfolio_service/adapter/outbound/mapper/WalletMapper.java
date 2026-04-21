package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.entity.WalletEntity;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    Wallet toDomain(WalletEntity entity);
    WalletEntity toEntity(Wallet domain);
}
