package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.events.mapper.AccountEventMapper;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountRequestConsumer {

    private final AccountRepositoryPort repository;
    private final AccountResponseProducer producer;
    private final AccountEventMapper mapper;
    private final AvroJsonDeserializer avroJsonDeserializer;

    @KafkaListener(topics = "${topic.account-request}", groupId = "account-service")
    public void consumeAccountRequest(String message) {
        log.info("[AccountRequestConsumer] Received raw message: {}", message);

        try {
            AccountRequestEvent event =
                    avroJsonDeserializer.deserialize(
                            message,
                            AccountRequestEvent.class,
                            AccountRequestEvent.getClassSchema()
                    );

            String correlationId = event.getCorrelationId().toString();
            String accountId = event.getAccountId().toString();

            log.info("[AccountRequestConsumer] Parsed AccountRequestEvent. correlationId={}, accountId={}",
                    correlationId, accountId);

            repository.findById(accountId)
                    .switchIfEmpty(Single.error(new RuntimeException("Account not found")))
                    .subscribe(
                            account -> {
                                log.info("[AccountRequestConsumer] Account found. accountId={}, correlationId={}",
                                        accountId, correlationId);

                                producer.publish(
                                        correlationId,
                                        mapper.toAccountResponseEvent(account, correlationId)
                                );

                                log.info("[AccountRequestConsumer] AccountResponseEvent published. correlationId={}",
                                        correlationId);
                            },
                            error -> {
                                log.warn("[AccountRequestConsumer] Account not found or error occurred. accountId={}, correlationId={}, error={}",
                                        accountId, correlationId, error.getMessage());

                                producer.publish(
                                        correlationId,
                                        mapper.toAccountNotFoundEvent(accountId, correlationId)
                                );

                                log.info("[AccountRequestConsumer] AccountNotFoundEvent published. correlationId={}",
                                        correlationId);
                            }
                    );

        } catch (Exception e) {
            log.error("[AccountRequestConsumer] Error processing AccountRequestEvent. rawMessage={}, error={}",
                    message, e.getMessage(), e);
        }
    }
}
