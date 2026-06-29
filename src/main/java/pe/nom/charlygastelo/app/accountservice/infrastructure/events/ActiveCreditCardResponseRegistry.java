package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ActiveCreditCardResponseRegistry {

    @Value("${active-credit-card.response.timeout:2}")
    private int timeout;

    private final Map<String, SingleEmitter<Boolean>> pendingRequests =
            new ConcurrentHashMap<>();

    public Single<Boolean> waitForResponse(String correlationId) {

        log.info(
                "Waiting ActiveCreditCardResponseEvent. correlationId={}, timeout={}s",
                correlationId,
                timeout
        );

        return Single.<Boolean>create(emitter ->
                        pendingRequests.put(correlationId, emitter)
                )
                .timeout(timeout, TimeUnit.SECONDS)
                .doFinally(() -> {
                    pendingRequests.remove(correlationId);

                    log.debug(
                            "Active credit card pending request removed. correlationId={}",
                            correlationId
                    );
                });
    }

    public void complete(String correlationId, boolean hasActiveCreditCard) {

        SingleEmitter<Boolean> emitter =
                pendingRequests.remove(correlationId);

        if (emitter == null) {
            log.warn(
                    "No pending active credit card request found. correlationId={}",
                    correlationId
            );
            return;
        }

        log.info(
                "Active credit card response received. correlationId={}, hasActiveCreditCard={}",
                correlationId,
                hasActiveCreditCard
        );

        emitter.onSuccess(hasActiveCreditCard);
    }
}