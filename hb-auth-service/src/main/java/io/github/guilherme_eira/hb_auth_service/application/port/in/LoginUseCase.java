package io.github.guilherme_eira.hb_auth_service.application.port.in;

import io.github.guilherme_eira.hb_auth_service.application.dto.LoginCommand;
import io.github.guilherme_eira.hb_auth_service.application.dto.LoginOutput;

public interface LoginUseCase {
    LoginOutput execute(LoginCommand cmd);
}
