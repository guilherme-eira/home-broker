package io.github.guilherme_eira.hb_auth_service.application.port.in;

import io.github.guilherme_eira.hb_auth_service.application.dto.RegisterCommand;
import io.github.guilherme_eira.hb_auth_service.application.dto.UserCreatedOutput;

public interface RegisterUseCase {
    UserCreatedOutput execute(RegisterCommand cmd);
}
