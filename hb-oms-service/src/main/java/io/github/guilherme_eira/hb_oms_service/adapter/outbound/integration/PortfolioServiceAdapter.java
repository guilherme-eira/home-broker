package io.github.guilherme_eira.hb_oms_service.adapter.outbound.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.guilherme_eira.hb_oms_service.adapter.common.dto.ErrorResponse;
import io.github.guilherme_eira.hb_oms_service.adapter.outbound.integration.dto.ReserveResourceRequest;
import io.github.guilherme_eira.hb_oms_service.application.dto.output.ReserveResourcePayload;
import io.github.guilherme_eira.hb_oms_service.application.exception.BusinessException;
import io.github.guilherme_eira.hb_oms_service.application.exception.IntegrationException;
import io.github.guilherme_eira.hb_oms_service.application.port.out.PortfolioServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioServiceAdapter implements PortfolioServicePort {

    private final PortfolioClient client;
    private final ObjectMapper objectMapper;

    @Override
    public void reserveResource(ReserveResourcePayload payload) {
        try {
            var request = new ReserveResourceRequest(
                    payload.investorId(),
                    payload.orderId(),
                    payload.side(),
                    payload.ticker(),
                    payload.volume()
            );
            client.reserveBalance(request);
        } catch (FeignException ex) {
            handleFeignException(ex);
        } catch (Exception ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }

    private void handleFeignException(FeignException e) {
        String content = e.contentUTF8();

        if (content == null || content.isBlank()) {
            throw new IntegrationException("Erro na integração com Portfolio: " + e.getMessage());
        }

        try {
            var error = objectMapper.readValue(content, ErrorResponse.class);
            if ("BUSINESS_RULE_VIOLATION".equals(error.code())) {
                throw new BusinessException(error.message());
            } else {
                throw new IntegrationException(error.message());
            }
        } catch (JsonProcessingException jsonEx) {

            throw new IntegrationException("Erro ao processar resposta de erro: " + content);
        }
    }
}
