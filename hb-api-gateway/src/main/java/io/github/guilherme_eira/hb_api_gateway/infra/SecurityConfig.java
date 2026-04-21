package io.github.guilherme_eira.hb_api_gateway.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/hb-auth-service/auth/**",
                                "/hb-portfolio-service/webhooks/**",
                                "/hb-auth-service/v3/api-docs/**",
                                "/hb-oms-service/v3/api-docs/**",
                                "/hb-portfolio-service/v3/api-docs/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}