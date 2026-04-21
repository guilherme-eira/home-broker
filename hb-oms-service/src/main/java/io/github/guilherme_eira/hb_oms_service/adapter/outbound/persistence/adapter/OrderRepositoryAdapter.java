package io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.adapter;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.mapper.OrderMapper;
import io.github.guilherme_eira.hb_oms_service.adapter.outbound.persistence.repository.OrderJpaRepository;
import io.github.guilherme_eira.hb_oms_service.application.port.out.OrderRepository;
import io.github.guilherme_eira.hb_oms_service.domain.enums.OrderStatus;
import io.github.guilherme_eira.hb_oms_service.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository repository;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(repository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID id){
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdWithLock(UUID id) {
        return repository.findByIdWithLock(id).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findByInvestorIdAndStatus(UUID investorId, OrderStatus status, Pageable pageable) {
        return repository.findByInvestorIdAndOrderStatus(investorId, status, pageable).map(mapper::toDomain);
    }

}
