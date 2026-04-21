package io.github.guilherme_eira.hb_auth_service.application.service;

import io.github.guilherme_eira.hb_auth_service.application.dto.LoginCommand;
import io.github.guilherme_eira.hb_auth_service.application.dto.LoginOutput;
import io.github.guilherme_eira.hb_auth_service.application.port.out.AuthServicePort;
import io.github.guilherme_eira.hb_auth_service.domain.model.UserCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthServicePort authServicePort;

    @InjectMocks
    private LoginService loginService;

    @Test
    void shouldReturnLoginOutputWhenCredentialsAreValid() {
        var cmd = new LoginCommand("guilherme", "senha123");
        var credential = new UserCredential("access-token", "refresh-token", "Bearer", 3600L);

        given(authServicePort.login(cmd.username(), cmd.password())).willReturn(credential);

        LoginOutput output = loginService.execute(cmd);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("access-token", output.accessToken());
        Assertions.assertEquals("refresh-token", output.refreshToken());
        Assertions.assertEquals(3600L, output.expiresIn());
    }

    @Test
    void shouldPropagateExceptionWhenAuthServiceFails() {
        var cmd = new LoginCommand("user_errado", "senha_errada");

        given(authServicePort.login(anyString(), anyString()))
                .willThrow(new BadCredentialsException("Usuário ou senha inválidos."));

        Assertions.assertThrows(BadCredentialsException.class, () -> loginService.execute(cmd));
    }
}