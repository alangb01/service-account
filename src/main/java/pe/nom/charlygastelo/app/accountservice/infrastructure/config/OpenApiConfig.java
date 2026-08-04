package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    private final OpenApiProperties properties;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = properties.getServers().stream()
                .map(s -> new Server()
                        .url(s.getUrl())
                        .description(s.getDescription()))
                .toList();

        return new OpenAPI().servers(servers);
    }
}