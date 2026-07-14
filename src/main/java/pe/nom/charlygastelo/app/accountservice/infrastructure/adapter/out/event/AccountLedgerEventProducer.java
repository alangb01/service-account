package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.AccountLedgerEventProducerMapper;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDepositOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountInitialDepositEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountWithdrawOccurredEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLedgerEventProducer implements AccountLedgerEventProducerPort {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final AccountLedgerEventProducerMapper mapper;

    @Value("${topic.account-withdraw-occurred}")
    private String accountWithdrawOccurred;

    @Value("${topic.account-deposit-occurred}")
    private String accountDepositOccurred;

    @Value("${topic.account-deposit-initial-occurred}")
    private String accountDepositInitialOcurred;

    @Override
    public Completable publishAccountWithdrawOccurred(AccountWithdrawOccurredEvent event) {
        return publish(
                accountWithdrawOccurred,
                event.getTransactionId().toString(),
                event
        );
    }

    @Override
    public Completable publishAccountDepositOccurred(AccountDepositOccurredEvent event) {
        return publish(
                accountDepositOccurred,
                event.getAccountId().toString(),
                event
        );
    }

    @Override
    public Completable publishAccountInitialDepositOccurred(AccountInitialDepositEvent event) {
        return publish(
                accountDepositInitialOcurred,
                event.getAccountId().toString(),
                event
        );
    }

    public Completable publish(String topic, String key, SpecificRecordBase event) {
        return Completable.create(emitter -> {
            try {

                log.info("[ACCOUNT-EVENT] Preparing event. topic={}, key={}, eventType={}",
                        topic, key, event.getClass().getSimpleName());

                log.debug("[ACCOUNT-EVENT] Serializing event. key={}, event={}",
                        key, event);


                log.debug("[ACCOUNT-EVENT] Payload serialized successfully. key={}, payload={}",
                        key, event);

                kafkaTemplate.send(topic, key, event)
                        .whenComplete((result, error) -> {
                            if (error != null) {
                                log.error("[ACCOUNT-EVENT] Error sending event. topic={}, key={}, reason={}",
                                        topic, key, error.getMessage(), error);
                                emitter.onError(error);
                                return;
                            }

                            log.info("[ACCOUNT-EVENT] Event sent successfully. topic={}, key={}, partition={}, offset={}",
                                    topic,
                                    key,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );

                            emitter.onComplete();
                        });

            } catch (Exception e) {
                log.error("[ACCOUNT-EVENT] Unexpected error serializing or sending event. topic={}, key={}, reason={}",
                        topic, key, e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    @Override
    public Completable publishAccountEventFor(Transaction tx, Account source, Account target) {

        return switch (tx.type()) {

            case CREDIT_PAYMENT,
                 DEBIT_CARD_PAYMENT,
                 WITHDRAW -> {
                var event = mapper.toAccountWithdrawOccurredEvent(tx, source);
                yield publishAccountWithdrawOccurred(event);
            }

            case YANKI_PAYMENT,
                 TRANSFER,
                 TRANSFER_TO_THIRD -> {
                var withdrawEvent = mapper.toAccountWithdrawOccurredEvent(tx,source);
                var depositEvent = mapper.toAccountDepositOccurredEvent(tx,target);

                yield publishAccountWithdrawOccurred(withdrawEvent)
                        .andThen(publishAccountDepositOccurred(depositEvent));
            }

            case DEPOSIT -> {
                var event = mapper.toAccountDepositOccurredEvent(tx,target);
                yield publishAccountDepositOccurred(event);
            }

            default -> Completable.complete();
        };
    }
}
