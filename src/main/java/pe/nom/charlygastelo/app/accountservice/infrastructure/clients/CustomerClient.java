package pe.nom.charlygastelo.app.accountservice.infrastructure.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.domain.port.CustomerClientPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.dto.CustomerResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.exception.CustomerServiceUnavailableException;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.mapper.CustomerClientMapper;
import reactor.core.publisher.Mono;

@Component
public class CustomerClient implements CustomerClientPort {
    private final WebClient webClient;
    private final CustomerClientMapper mapper;

    public CustomerClient(WebClient.Builder builder,
                         @Value("${client.customer-service.base-url}") String baseUrl,
                          CustomerClientMapper clientMapper) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.mapper = clientMapper;
    }

    @CircuitBreaker(name = "customerservice", fallbackMethod = "fallbackAccount")
    @TimeLimiter(name = "customerservice")
    @Retry(name = "customerservice")
    @Override
    public Single<Customer> getById(String customerId) {

        return Single.fromPublisher(
                        webClient.get()
                                .uri("/customers/{id}", customerId)
                                .retrieve()
                                .bodyToMono(CustomerResponse.class)
                )

                // 1. Convertir 404 → CustomerNotFoundException
                .onErrorResumeNext(e -> {
                    if (e instanceof WebClientResponseException.NotFound) {
                        return Single.error(new CustomerNotFoundException(
                                "Cliente no encontrado: " + customerId
                        ));
                    }
                    return Single.error(e);
                })

                // 2. Servicio caído → CustomerServiceUnavailableException
                .onErrorResumeNext(e -> {
                    if (e instanceof WebClientRequestException) {
                        return Single.error(new CustomerServiceUnavailableException(
                                "Customer service no disponible"
                        ));
                    }
                    return Single.error(e);
                })

                // 3. Mapear al dominio
                .map(mapper::toDomain);
    }

    private Single<Customer> fallbackAccount(String customerId, Throwable throwable) {
        return Single.error(new CustomerServiceUnavailableException("Customer service no disponible"));
    }
}
