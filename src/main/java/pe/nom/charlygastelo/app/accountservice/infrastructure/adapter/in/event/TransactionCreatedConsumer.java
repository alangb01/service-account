package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessCreatedTransactionUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper.TransactionEventConsumerMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedConsumer {

    private final ProcessCreatedTransactionUseCase processCreatedTransactionUseCase;
    private final TransactionEventConsumerMapper transactionMapper;
    private final TransactionEventProducer transactionEventProducer;
    private final AccountLedgerEventProducerPort accountLedgerEventProducer;

    @KafkaListener(topics = "${topic.transaction-created}", groupId = "account-service")
    public void consume(TransactionCreatedEvent event) {

        try {
            Transaction tx = transactionMapper.toDomain(event);

            log.info("[ACCOUNT] Received TRANSACTION_CREATED txId={}, type={}", tx.id(), tx.type());

            // 1. Filtrar transacciones que NO procesa account-service
            if (!processCreatedTransactionUseCase.isAccountServiceResponsible(tx.type())) {
                log.info("[ACCOUNT] Ignoring txId={} type={} (not account responsibility)", tx.id(), tx.type());
                return;
            }

            // 2. Ejecutar el caso de uso  (devuelve ProcessedTransaction)
            processCreatedTransactionUseCase.execute(tx)
                    .subscribe(
                            result -> {

                                log.info("[ACCOUNT] Step completed for txId={}", tx.id());

                                // 3. Publicar evento ledger según el tipo de transacción
                                accountLedgerEventProducer.publishAccountEventFor(
                                        result.transaction(),
                                        result.source(),
                                        result.target()
                                ).subscribe(
                                        () -> log.info("[ACCOUNT] Ledger event published for txId={}", tx.id()),
                                        err -> log.error("[ACCOUNT] Failed to publish ledger event for txId={}, reason={}",
                                                tx.id(), err.getMessage())
                                );
                            },
                            error -> {
                                log.error("[ACCOUNT] Failed txId={}, reason={}", tx.id(), error.getMessage());

                                transactionEventProducer.publishTransactionFailed(tx, error.getMessage())
                                        .subscribe(
                                                () -> log.info("[ACCOUNT] FAILED event published txId={}", tx.id()),
                                                err2 -> log.error("[ACCOUNT] FAILED event could not be published txId={}, reason={}", tx.id(), err2.getMessage())
                                        );
                            }
                    );

        } catch (Exception ex) {
            log.error("[ACCOUNT] Fatal error: {}", ex.getMessage());
        }
    }
}
