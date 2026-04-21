package io.github.guilherme_eira.hb_auth_service.application.service;

import io.github.guilherme_eira.hb_auth_service.application.dto.RegisterCommand;
import io.github.guilherme_eira.hb_auth_service.application.dto.UserCreatedOutput;
import io.github.guilherme_eira.hb_auth_service.application.port.out.AuthServicePort;
import io.github.guilherme_eira.hb_auth_service.application.port.out.EventPublisherPort;
import io.github.guilherme_eira.hb_auth_service.domain.exception.BusinessException;
import io.github.guilherme_eira.hb_auth_service.domain.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private AuthServicePort authService;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private RegisterService registerService;

    @Test
    void shouldRegisterAndPublishEventSuccessfully() {
        var cmd = new RegisterCommand("guieira", "gui@email.com", "74645828371", "senha123", "Gui", "Eira");
        String generatedUserId = "uuid-keycloak-789";

        given(authService.register(any(User.class))).willReturn(generatedUserId);

        registerService.execute(cmd);

        InOrder inOrder = inOrder(authService, eventPublisher);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        inOrder.verify(authService).register(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("guieira", capturedUser.username());
        assertEquals("74645828371", capturedUser.taxId());

        ArgumentCaptor<UserCreatedOutput> eventCaptor = ArgumentCaptor.forClass(UserCreatedOutput.class);
        inOrder.verify(eventPublisher).publishUserCreated(eventCaptor.capture());

        UserCreatedOutput capturedEvent = eventCaptor.getValue();
        assertEquals(generatedUserId, capturedEvent.userId());
        assertEquals("Gui Eira", capturedEvent.fullName());
    }

    @Test
    void shouldNotPublishEventWhenAuthServiceFails() {
        var cmd = new RegisterCommand("guieira", "gui@email.com", "74645828371", "pass", "Gui", "Eira");

        when(authService.register(any(User.class)))
                .thenThrow(new BusinessException("Usuário já existe"));

        Assertions.assertThrows(BusinessException.class, () -> registerService.execute(cmd));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldFailBeforeAnyPortWhenTaxIdIsInvalid() {
        var cmd = new RegisterCommand("u", "e@e.com", "1234567890", "p", "F", "L");

        Assertions.assertThrows(BusinessException.class, () -> registerService.execute(cmd));

        verifyNoInteractions(authService);
        verifyNoInteractions(eventPublisher);
    }
}