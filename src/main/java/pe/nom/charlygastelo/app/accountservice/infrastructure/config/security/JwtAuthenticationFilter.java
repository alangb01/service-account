package pe.nom.charlygastelo.app.accountservice.infrastructure.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String userId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id");

        String customerId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Customer-Id");

        String rolesHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Roles");

        if (userId == null || rolesHeader == null || rolesHeader.isBlank()) {
            return chain.filter(exchange);
        }

        List<String> roles = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();

        UserPrincipal principal = new UserPrincipal(
                userId,
                customerId,
                roles
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .toList()
                );

        SecurityContextImpl securityContext =
                new SecurityContextImpl(authentication);

        return chain.filter(exchange)
                .contextWrite(
                        ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(securityContext)
                        )
                );
    }
}