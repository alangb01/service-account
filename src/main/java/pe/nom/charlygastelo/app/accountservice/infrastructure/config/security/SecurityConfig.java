package pe.nom.charlygastelo.app.accountservice.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final SecurityPermitPathsProperties securityPermitPathsProperties;
    private final JwtValidationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        log.debug(" FILTER ");
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(securityPermitPathsProperties.getPermitPaths().toArray(String[]::new)).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}