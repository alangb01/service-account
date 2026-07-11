package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.ServiceUnavailableException;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CardClientPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.client.dto.ActiveCreditCardResponse;

@Component
public class CardClient implements CardClientPort {
    private final WebClient webClient;

    public CardClient(WebClient.Builder builder,
                          @Value("${client.card-service.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @CircuitBreaker(name = "cardservice", fallbackMethod = "fallbackHasActiveCreditCard")
    @TimeLimiter(name = "cardservice")
    @Retry(name = "cardservice")
    @Override
    public Single<Boolean> hasActiveCreditCard(String customerId, String token) {
        System.out.println("[CardClient] getById: customerId"+customerId);
        return Single.fromPublisher(
                        webClient.get()
                                .uri("/customers/{id}/credit-cards/active", customerId)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve()
                                .bodyToMono(ActiveCreditCardResponse.class)
                )

                // 1. Convertir 404 → CustomerNotFoundException
                .onErrorResumeNext(e -> {
                    if (e instanceof WebClientResponseException.NotFound) {
                        return Single.error(new CustomerNotFoundException(
                                "Credit card not found: " + customerId
                        ));
                    }
                    return Single.error(e);
                })

                // 2. Servicio caído → CustomerServiceUnavailableException
                .onErrorResumeNext(e -> {
                    if (e instanceof WebClientRequestException) {
                        return Single.error(new ServiceUnavailableException(
                                "Card service unavailable"
                        ));
                    }
                    return Single.error(e);
                }).map(ActiveCreditCardResponse::hasActiveCreditCard);
    }

    public Single<Boolean> fallbackHasActiveCreditCard(String id, String token) {
        return Single.error(new ServiceUnavailableException("Card service unavailable"));
    }
}
