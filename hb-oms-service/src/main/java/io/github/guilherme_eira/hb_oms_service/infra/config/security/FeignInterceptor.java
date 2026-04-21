package io.github.guilherme_eira.hb_oms_service.infra.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import feign.RequestInterceptor;

@Configuration
public class FeignInterceptor {

    @Bean
    public RequestInterceptor requestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        return requestTemplate -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("portfolio-client")
                    .principal("hb-oms-service")
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                String token = authorizedClient.getAccessToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
