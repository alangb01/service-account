package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.ServiceUnavailableException;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CreditClientPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.client.dto.OverdueDebtResponse;

@Component
@Slf4j
public class CreditClient implements CreditClientPort {
    private final WebClient webClient;

    public CreditClient(WebClient.Builder builder,
                      @Value("${client.credit-service.base-url}") String baseUrl
        ) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @CircuitBreaker(name = "creditservice", fallbackMethod = "fallbackHasOverdueDebt")
    @TimeLimiter(name = "creditservice")
    @Retry(name = "creditservice")
    @Override
    public Single<Boolean> hasOverdueDebt(String customerId, String token) {
        log.info("[CreditClient] getById: customerId {} token {}",customerId, token);
        return Single.fromPublisher(
                        webClient.get()
                                .uri("/customers/{customerId}/credits/overdue", customerId)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve()
                                .bodyToMono(OverdueDebtResponse.class)

                )

                // 1. Convertir 404 → CustomerNotFoundException
                .onErrorResumeNext(e -> {
                    if (e instanceof WebClientResponseException.NotFound) {
                        return Single.error(new CustomerNotFoundException(
                                "Credit not found: " + customerId
                        ));
                    }
                    return Single.error(e);
                })

                // 2. Servicio caído → CustomerServiceUnavailableException
                .onErrorResumeNext(e -> {
                    if (e instanceof WebClientRequestException) {
                        return Single.error(new ServiceUnavailableException(
                                "Credit service no disponible"
                        ));
                    }
                    return Single.error(e);
                })
                .map(OverdueDebtResponse::hasOverdueDebt);
    }

    public Single<Boolean> fallbackHasOverdueDebt(String id, String token) {
        return Single.error(new ServiceUnavailableException("Credit service no disponible"));
    }
}
