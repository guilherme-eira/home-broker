package io.github.guilherme_eira.hb_oms_service.infra.config.general;

import io.github.guilherme_eira.hb_oms_service.domain.service.MarketSessionValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public MarketSessionValidator marketSessionValidator() {
        return new MarketSessionValidator();
    }
}
