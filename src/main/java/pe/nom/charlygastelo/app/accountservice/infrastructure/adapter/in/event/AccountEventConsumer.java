package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountResponseProducer;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountRequestEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    private final FindAccountUseCasePort findAccountAdapter;
    private final AccountResponseProducer responseProducer;
    private final AvroJsonDeserializer deserializer;
    private final AccountEventConsumerMapper mapper;

    @KafkaListener(topics = "${topic.account-request}", groupId = "account-service")
    public void consume(String message) {
        log.info("[ACCOUNT-REQUEST] Message received from Kafka.");

        try {
            log.debug("[ACCOUNT-REQUEST] Deserializing AccountRequestEvent. rawMessage={}", message);

            AccountRequestEvent event = deserializer.deserialize(
                    message,
                    AccountRequestEvent.class,
                    AccountRequestEvent.getClassSchema()
            );

            String correlationId = event.getCorrelationId().toString();
            String accountId = event.getAccountId().toString();

            log.info("[ACCOUNT-REQUEST] Event deserialized successfully. correlationId={}, accountId={}",
                    correlationId, accountId);

            findAccountAdapter.findById(accountId)
                    .subscribe(
                            account -> {
                                log.info("[CUSTOMER-REQUEST] Account found. correlationId={}, accountId={}",
                                        correlationId, accountId);

                                responseProducer.publish(
                                        correlationId,
                                        mapper.toAccountResponseEvent(account, correlationId)
                                );

                                log.info("[CUSTOMER-RESPONSE] AccountResponseEvent published. " +
                                                "correlationId={}, accountId={}",
                                        correlationId, accountId);
                            },
                            error -> {
                                log.error("[CUSTOMER-REQUEST] Error searching account. correlationId={}, " +
                                                "accountId={}, reason={}",
                                        correlationId, accountId, error.getMessage(), error);

                                responseProducer.publish(
                                        correlationId,
                                        mapper.toAccountNotFoundEvent(accountId, correlationId)
                                );

                                log.warn("[CUSTOMER-RESPONSE] AccountNotFoundEvent published due to error. " +
                                                "correlationId={}, accountId={}",
                                        correlationId, accountId);
                            },
                            () -> {
                                log.warn("[CUSTOMER-REQUEST] Account not found. correlationId={}, accountId={}",
                                        correlationId, accountId);

                                responseProducer.publish(
                                        correlationId,
                                        mapper.toAccountNotFoundEvent(accountId, correlationId)
                                );

                                log.info("[CUSTOMER-RESPONSE] AccountNotFoundEvent published. " +
                                                "correlationId={}, accountId={}",
                                        correlationId, accountId);
                            }
                    );

        } catch (Exception e) {
            log.error("[ACCOUNT-REQUEST] Fatal error processing AccountRequestEvent. reason={}", e.getMessage(), e);
        }
    }

    private void publish(
            Completable completable,
            String successMsg,
            String errorMsg,
            String correlationId,
            String accountId
    ) {
        completable.subscribe(
                () -> log.info("{} correlationId={}, accountId={}",
                        successMsg, correlationId, accountId),
                error -> log.error("{} correlationId={}, accountId={}, reason={}",
                        errorMsg, correlationId, accountId, error.getMessage(), error)
        );
    }
}
