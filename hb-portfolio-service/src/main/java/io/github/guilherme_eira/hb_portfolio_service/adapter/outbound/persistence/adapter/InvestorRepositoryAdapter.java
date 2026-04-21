package io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper.InvestorMapper;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.persistence.repository.InvestorJpaRepository;
import io.github.guilherme_eira.hb_portfolio_service.application.port.out.InvestorRepository;
import io.github.guilherme_eira.hb_portfolio_service.domain.model.Investor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InvestorRepositoryAdapter implements InvestorRepository {

    private final InvestorJpaRepository repository;
    private final InvestorMapper mapper;

    @Override
    public Investor save(Investor investor) {
        return mapper.toDomain(repository.save(mapper.toEntity(investor)));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
