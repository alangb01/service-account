package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountBusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Card;
import pe.nom.charlygastelo.app.shared.avro.dto.CardResponseEvent;

@Slf4j
@Component
public class CardResponseRegistry {

    @Value("${card.response.timeout:2}")
    private int timeout;

    private final Map<String, SingleEmitter<Card>> pendingRequests =
            new ConcurrentHashMap<>();

    public Single<Card> waitForResponse(String correlationId) {
        log.info("Waiting CardResponseEvent. correlationId={}, timeout={}s",
                correlationId, timeout);

        return Single.<Card>create(emitter ->
                        pendingRequests.put(correlationId, emitter)
                )
                .timeout(timeout, TimeUnit.SECONDS)
                .doFinally(() -> {
                    pendingRequests.remove(correlationId);
                    log.debug("Card pending request removed. correlationId={}",
                            correlationId);
                });
    }

    public void complete(CardResponseEvent event) {
        String correlationId = event.getCorrelationId().toString();

        SingleEmitter<Card> emitter =
                pendingRequests.remove(correlationId);

        if (emitter == null) {
            log.warn("No pending card request found. correlationId={}",
                    correlationId);
            return;
        }

        if (!event.getFound()) {
            log.warn("Card not found from response. correlationId={}, cardId={}",
                    correlationId,
                    event.getCardId());

            emitter.onError(
                    new AccountBusinessException("Card not found")
            );
            return;
        }

        Card card = new Card(
                event.getCardId().toString(),
                event.getCustomerId().toString(),
                event.getAccountId().toString(),
                event.getCardNumber().toString(),
                event.getCardType().toString(),
                event.getStatus().toString()
        );

        log.info("Card response completed successfully. correlationId={}, cardId={}",
                correlationId,
                card.id());

        emitter.onSuccess(card);
    }
}