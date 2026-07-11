package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessCreatedTransactionUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper.AccountEventConsumerMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper.TransactionEventConsumerMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountResponseProducer;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountRequestEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    private final FindAccountUseCasePort findAccountAdapter;
    private final AccountResponseProducer responseProducer;
    private final AccountEventConsumerMapper accountMapper;

    @KafkaListener(topics = "${topic.account-request}", groupId = "account-service")
    public void consumeAccountRequest(AccountRequestEvent event) {
        log.info("[ACCOUNT-REQUEST] Message received from Kafka.");

        try {
            log.debug("[ACCOUNT-REQUEST] Deserializing AccountRequestEvent. rawMessage={}", event.toString());



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
                                        accountMapper.toAccountResponseEvent(account, correlationId)
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
                                        accountMapper.toAccountNotFoundEvent(accountId, correlationId)
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
                                        accountMapper.toAccountNotFoundEvent(accountId, correlationId)
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

}
