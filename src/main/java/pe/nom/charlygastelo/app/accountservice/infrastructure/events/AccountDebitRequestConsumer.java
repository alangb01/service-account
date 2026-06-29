package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.InsufficientBalanceException;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDebitRequestEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDebitResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDebitRequestConsumer {

    private final AvroJsonDeserializer deserializer;
    private final AccountRepositoryPort repository;
    private final AccountDebitResponseProducer responseProducer;

    @KafkaListener(
            topics = "${topic.account-debit-request}",
            groupId = "account-service")
    public void consume(String message) {
        log.debug("AccountDebitRequestEvent raw message received");

        try {
            AccountDebitRequestEvent event =
                    deserializer.deserialize(
                            message,
                            AccountDebitRequestEvent.class,
                            AccountDebitRequestEvent.getClassSchema()
                    );

            String correlationId = event.getCorrelationId().toString();
            String transactionId = event.getTransactionId().toString();
            String accountId = event.getAccountId().toString();

            log.info(
                    "AccountDebitRequestEvent received. correlationId={}, transactionId={}, accountId={}, amount={}",
                    correlationId,
                    transactionId,
                    accountId,
                    event.getAmount()
            );

            repository.findById(accountId)
                    .switchIfEmpty(
                            io.reactivex.rxjava3.core.Single.error(
                                    new AccountNotFoundException(
                                            "Account not found: " + accountId
                                    )
                            )
                    )
                    .flatMap(account -> {
                        BigDecimal amount = BigDecimal.valueOf(event.getAmount());

                        if (!account.isActive()) {
                            return io.reactivex.rxjava3.core.Single.error(
                                    new IllegalStateException("Account is inactive")
                            );
                        }

                        if (account.balance().compareTo(amount) < 0) {
                            return io.reactivex.rxjava3.core.Single.error(
                                    new InsufficientBalanceException(
                                            "Insufficient balance"
                                    )
                            );
                        }

                        return repository.save(
                                account.withBalance(
                                        account.balance().subtract(amount)
                                )
                        );
                    })
                    .subscribe(
                            updated -> {
                                log.info(
                                        "Account debit completed. correlationId={}, transactionId={}, accountId={}, balanceAfter={}",
                                        correlationId,
                                        transactionId,
                                        updated.id(),
                                        updated.balance()
                                );

                                responseProducer.publish(
                                        correlationId,
                                        successResponse(
                                                event,
                                                updated.balance().doubleValue()
                                        )
                                );
                            },
                            error -> {
                                log.error(
                                        "Account debit failed. correlationId={}, transactionId={}, accountId={}, reason={}",
                                        correlationId,
                                        transactionId,
                                        accountId,
                                        error.getMessage(),
                                        error
                                );

                                responseProducer.publish(
                                        correlationId,
                                        failedResponse(
                                                event,
                                                error.getMessage()
                                        )
                                );
                            }
                    );

        } catch (Exception e) {
            log.error(
                    "Error processing AccountDebitRequestEvent. reason={}",
                    e.getMessage(),
                    e
            );
        }
    }

    private AccountDebitResponseEvent successResponse(
            AccountDebitRequestEvent request,
            double balanceAfter) {

        return AccountDebitResponseEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_DEBIT_RESPONSE")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setCorrelationId(request.getCorrelationId().toString())
                .setTransactionId(request.getTransactionId().toString())
                .setCustomerId(request.getCustomerId().toString())
                .setAccountId(request.getAccountId().toString())
                .setSuccess(true)
                .setReason("")
                .setBalanceAfter(balanceAfter)
                .build();
    }

    private AccountDebitResponseEvent failedResponse(
            AccountDebitRequestEvent request,
            String reason) {

        return AccountDebitResponseEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_DEBIT_RESPONSE")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setCorrelationId(request.getCorrelationId().toString())
                .setTransactionId(request.getTransactionId().toString())
                .setCustomerId(request.getCustomerId().toString())
                .setAccountId(request.getAccountId().toString())
                .setSuccess(false)
                .setReason(reason == null ? "" : reason)
                .setBalanceAfter(0.0)
                .build();
    }
}