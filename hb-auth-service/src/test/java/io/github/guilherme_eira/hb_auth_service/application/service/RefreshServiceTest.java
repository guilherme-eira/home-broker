package io.github.guilherme_eira.hb_auth_service.application.service;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private AuthServicePort authServicePort;

    @InjectMocks
    private RefreshService refreshService;

    @Test
    void shouldReturnLoginOutputWhenRefreshTokenIsValid() {
        String oldRefreshToken = "old-refresh-token";
        var credential = new UserCredential("new-access", "new-refresh", "Bearer", 3600L);

        given(authServicePort.refresh(oldRefreshToken)).willReturn(credential);

        LoginOutput output = refreshService.execute(oldRefreshToken);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("new-access", output.accessToken());
        Assertions.assertEquals("new-refresh", output.refreshToken());
    }

    @Test
    void shouldPropagateExceptionWhenTokenIsExpired() {
        String expiredToken = "expired-token";

        when(authServicePort.refresh(expiredToken))
                .thenThrow(new BadCredentialsException("Sessão expirada ou token inválido."));

        var exception = Assertions.assertThrows(BadCredentialsException.class,
                () -> refreshService.execute(expiredToken));

        Assertions.assertEquals("Sessão expirada ou token inválido.", exception.getMessage());
    }
}