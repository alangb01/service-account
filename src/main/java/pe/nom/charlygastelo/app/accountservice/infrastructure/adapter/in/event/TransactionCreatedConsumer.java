package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.CreatedTransactionUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper.TransactionEventConsumerMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedConsumer {

    private final CreatedTransactionUseCase processCreatedTransactionUseCase;
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
            processCreatedTransactionUseCase.execute(tx).subscribe();

        } catch (Exception ex) {
            log.error("[ACCOUNT] Fatal error: {}", ex.getMessage());
        }
    }
}
