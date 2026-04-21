package io.github.guilherme_eira.hb_auth_service.adapter.outbound.amqp;

import io.github.guilherme_eira.hb_auth_service.adapter.outbound.amqp.dto.UserCreatedEvent;
import io.github.guilherme_eira.hb_auth_service.application.dto.UserCreatedOutput;
import io.github.guilherme_eira.hb_auth_service.application.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class EventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishUserCreated(UserCreatedOutput output) {
        var event = new UserCreatedEvent(
                output.userId(),
                output.fullName(),
                output.email(),
                output.taxId(),
                output.username(),
                output.createdAt()
        );
        rabbitTemplate.convertAndSend("auth.user-events.direct", "user.created", event);

        log.info("Evento de criação de usuário enviado | Usuário: {} | ID: {} | Email: {}",
                output.username(), output.userId(), output.email());
    }
}
