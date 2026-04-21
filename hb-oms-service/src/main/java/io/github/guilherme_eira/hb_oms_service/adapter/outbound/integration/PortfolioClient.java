package io.github.guilherme_eira.hb_oms_service.adapter.outbound.integration;

import io.github.guilherme_eira.hb_oms_service.adapter.outbound.integration.dto.ReserveResourceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hb-portfolio-service", url = "${hb-portfolio-service.url:}")
public interface PortfolioClient {

    @PostMapping(value = "/wallets/reserve", consumes = MediaType.APPLICATION_JSON_VALUE)
    void reserveBalance(@RequestBody ReserveResourceRequest req);

}
