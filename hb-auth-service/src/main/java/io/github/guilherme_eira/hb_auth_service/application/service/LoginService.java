package io.github.guilherme_eira.hb_auth_service.application.service;

import io.github.guilherme_eira.hb_auth_service.application.dto.LoginCommand;
import io.github.guilherme_eira.hb_auth_service.application.dto.LoginOutput;
import io.github.guilherme_eira.hb_auth_service.application.port.in.LoginUseCase;
import io.github.guilherme_eira.hb_auth_service.application.port.out.AuthServicePort;
import io.github.guilherme_eira.hb_auth_service.domain.model.UserCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final AuthServicePort authService;

    @Override
    public LoginOutput execute(LoginCommand cmd) {
        UserCredential credential = authService.login(cmd.username(), cmd.password());
        return new LoginOutput(
                credential.accessToken(),
                credential.refreshToken(),
                credential.tokenType(),
                credential.expiresIn()
        );
    }
}
