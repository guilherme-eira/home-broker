package io.github.guilherme_eira.hb_auth_service.infra.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Home Broker - Auth Service")
                        .version("1.0")
                        .description("Serviço responsável pela autenticação e gestão de usuários via Keycloak. " +
                                "Inclui fluxos de login, registro e renovação de tokens."));
    }
}
