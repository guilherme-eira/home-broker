package io.github.guilherme_eira.hb_auth_service.application.service;

import io.github.guilherme_eira.hb_auth_service.application.dto.RegisterCommand;
import io.github.guilherme_eira.hb_auth_service.application.dto.UserCreatedOutput;
import io.github.guilherme_eira.hb_auth_service.application.port.in.RegisterUseCase;
import io.github.guilherme_eira.hb_auth_service.application.port.out.AuthServicePort;
import io.github.guilherme_eira.hb_auth_service.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_auth_service.domain.model.User;
import io.github.guilherme_eira.hb_auth_service.domain.vo.TaxId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final AuthServicePort authService;
    private final EventPublisherPort eventPublisher;

    @Override
    public UserCreatedOutput execute(RegisterCommand cmd) {

        var validTaxId = new TaxId(cmd.taxId());

        var user = new User(
                cmd.username(),
                cmd.email(),
                validTaxId.getValue(),
                cmd.firstName(),
                cmd.lastName(),
                cmd.password()
        );
        var userId = authService.register(user);

        UserCreatedOutput output = new UserCreatedOutput(
                userId,
                user.getFullName(),
                user.email(),
                user.taxId(),
                user.username(),
                Instant.now()
        );

        eventPublisher.publishUserCreated(output);

        return output;
    }
}
