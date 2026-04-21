package io.github.guilherme_eira.hb_auth_service.application.service;

import io.github.guilherme_eira.hb_auth_service.application.dto.LoginOutput;
import io.github.guilherme_eira.hb_auth_service.application.port.in.RefreshUseCase;
import io.github.guilherme_eira.hb_auth_service.application.port.out.AuthServicePort;
import io.github.guilherme_eira.hb_auth_service.domain.model.UserCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService implements RefreshUseCase {

    private final AuthServicePort authService;

    @Override
    public LoginOutput execute(String refreshToken) {
        UserCredential credential = authService.refresh(refreshToken);
        return new LoginOutput(
                credential.accessToken(),
                credential.refreshToken(),
                credential.tokenType(),
                credential.expiresIn()
        );
    }
}
