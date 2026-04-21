package io.github.guilherme_eira.hb_oms_service.application.port.in;

import io.github.guilherme_eira.hb_oms_service.application.dto.input.ProcessExecutionCommand;

public interface ProcessExecutionUseCase {
    void execute(ProcessExecutionCommand cmd);
}
