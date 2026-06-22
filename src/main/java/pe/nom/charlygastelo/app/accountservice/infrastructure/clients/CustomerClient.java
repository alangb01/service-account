package pe.nom.charlygastelo.app.accountservice.infrastructure.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.dto.CustomerResponse;
import reactor.core.publisher.Mono;

@Component
public class CustomerClient {
    private final WebClient webClient;

    public CustomerClient(WebClient.Builder builder,
                         @Value("${client.customer-service.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @CircuitBreaker(name = "customerservice", fallbackMethod = "fallbackAccount")
    @TimeLimiter(name = "customerservice")
    @Retry(name = "customerservice")
    public Mono<CustomerResponse> getCustomer(String customerId) {
        return webClient.get()
                .uri("/api/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerResponse.class);
    }

    private Mono<CustomerResponse> fallbackAccount(String customerId, Throwable ex) {
        return Mono.just(new CustomerResponse(
                customerId,
                null,
                null,
                null,
                null,
                null,
                "CUSTOMER_SERVICE_UNAVAILABLE"
        ));
    }
}
