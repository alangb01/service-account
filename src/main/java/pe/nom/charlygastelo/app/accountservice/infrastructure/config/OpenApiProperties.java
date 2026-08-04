package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

    private List<ServerProperties> servers;

    public List<ServerProperties> getServers() {
        return servers;
    }

    public void setServers(List<ServerProperties> servers) {
        this.servers = servers;
    }

    public static class ServerProperties {
        private String url;
        private String description;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
