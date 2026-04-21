package io.github.guilherme_eira.hb_portfolio_service.application.port.in;

import io.github.guilherme_eira.hb_portfolio_service.application.dto.ResourcesOutput;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetResourcesUseCase {
    ResourcesOutput execute(UUID userId, Pageable pageable);
}
