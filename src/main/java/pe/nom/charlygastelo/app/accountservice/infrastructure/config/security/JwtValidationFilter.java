package pe.nom.charlygastelo.app.accountservice.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pe.nom.charlygastelo.app.accountservice.domain.port.AuthRepositoryPort;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component("jwtValidationFilter")
public class JwtValidationFilter implements WebFilter {

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final SecurityPermitPathsProperties securityPermitPathsProperties;

    private final AuthRepositoryPort authClient; // WebClient hacia auth-service

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info("path requested {}", path);

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No bearer");
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        log.info("bearer {}", token);
        return authClient.validate(token)
                .flatMap(response -> {

                    if (!response.valid()) {
                        return unauthorized(exchange);
                    }

                    UserPrincipal principal = new UserPrincipal(
                            response.userId(),
                            response.customerId(),
                            response.roles()
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    response.roles().stream()
                                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                            .toList()
                            );

                    SecurityContextImpl context = new SecurityContextImpl(authentication);

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
                });
    }

    private boolean isPublicPath(String path) {

        for (String pattern : securityPermitPathsProperties.getPermitPaths()) {
            if (matcher.match(pattern, path)) {
                log.info("public path found {} ", path );
                return true;
            }
        }

        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        log.debug ("sin acceso");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
