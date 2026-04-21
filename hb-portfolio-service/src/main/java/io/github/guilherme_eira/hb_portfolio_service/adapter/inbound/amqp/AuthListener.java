package io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.amqp;

import io.github.guilherme_eira.hb_portfolio_service.adapter.inbound.amqp.dto.UserCreatedEvent;
import io.github.guilherme_eira.hb_portfolio_service.adapter.outbound.mapper.InvestorMapper;
import io.github.guilherme_eira.hb_portfolio_service.application.dto.CreateInvestorCommand;
import io.github.guilherme_eira.hb_portfolio_service.application.port.in.CreateInvestorUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class AuthListener {

    private final CreateInvestorUseCase createInvestorUseCase;
    private final InvestorMapper mapper;

    @RabbitListener(queues = "portfolio.user-created.queue")
    public void onUserCreated(UserCreatedEvent event) {
        var cmd = new CreateInvestorCommand(
                event.userId(),
                event.fullName(),
                event.email(),
                event.taxId(),
                event.username(),
                event.createdAt()
        );

        log.info("Evento de criação recebido | Processando abertura de carteira | Usuário: {} | ID: {}",
                event.username(), event.userId());

        createInvestorUseCase.execute(cmd);
    }
}
