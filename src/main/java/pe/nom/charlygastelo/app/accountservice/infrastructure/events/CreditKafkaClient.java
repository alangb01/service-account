package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.time.Instant;
import java.util.UUID;

import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.port.CreditEventPort;
import pe.nom.charlygastelo.app.shared.avro.dto.ActiveCreditCardRequestEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.OverdueDebtRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditKafkaClient implements CreditEventPort {

    private final OverdueDebtRequestProducer overdueDebtRequestProducer;
    private final OverdueDebtResponseRegistry overdueDebtResponseRegistry;

    private final ActiveCreditCardRequestProducer activeCreditCardRequestProducer;
    private final ActiveCreditCardResponseRegistry activeCreditCardResponseRegistry;

    @Override
    public Single<Boolean> hasOverdueDebt(String customerId) {
        String correlationId = UUID.randomUUID().toString();

        OverdueDebtRequestEvent event =
                OverdueDebtRequestEvent.newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setEventType("OVERDUE_DEBT_REQUEST")
                        .setOccurredAt(Instant.now().toString())
                        .setVersion("1.0")
                        .setSource("account-service")
                        .setCorrelationId(correlationId)
                        .setCustomerId(customerId)
                        .build();

        log.info("Requesting overdue debt validation. customerId={}, correlationId={}",
                customerId, correlationId);

        return overdueDebtResponseRegistry.waitForResponse(correlationId)
                .doOnSubscribe(disposable ->
                        overdueDebtRequestProducer.send(correlationId, event)
                )
                .doOnSuccess(hasDebt ->
                        log.info("Overdue debt validation completed. customerId={}, correlationId={}, hasDebt={}",
                                customerId, correlationId, hasDebt))
                .doOnError(error ->
                        log.error("Overdue debt validation failed. customerId={}, correlationId={}, reason={}",
                                customerId, correlationId, error.getMessage(), error));
    }

    @Override
    public Single<Boolean> hasActiveCreditCard(String customerId) {
        String correlationId = UUID.randomUUID().toString();

        ActiveCreditCardRequestEvent event =
                ActiveCreditCardRequestEvent.newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setEventType("ACTIVE_CREDIT_CARD_REQUEST")
                        .setOccurredAt(Instant.now().toString())
                        .setVersion("1.0")
                        .setSource("account-service")
                        .setCorrelationId(correlationId)
                        .setCustomerId(customerId)
                        .build();

        log.info("Requesting active credit card validation. customerId={}, correlationId={}",
                customerId, correlationId);

        return activeCreditCardResponseRegistry.waitForResponse(correlationId)
                .doOnSubscribe(disposable ->
                        activeCreditCardRequestProducer.send(correlationId, event)
                )
                .doOnSuccess(hasCard ->
                        log.info("Active credit card validation completed. customerId={}, correlationId={}, hasActiveCreditCard={}",
                                customerId, correlationId, hasCard))
                .doOnError(error ->
                        log.error("Active credit card validation failed. customerId={}, correlationId={}, reason={}",
                                customerId, correlationId, error.getMessage(), error));
    }
}