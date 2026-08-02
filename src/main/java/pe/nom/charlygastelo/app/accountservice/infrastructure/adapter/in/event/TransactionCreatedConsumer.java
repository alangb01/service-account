package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.CreatedTransactionUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.TransactionCommand;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper.TransactionEventConsumerMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedConsumer {

    private final CreatedTransactionUseCase processCreatedTransactionUseCase;
    private final TransactionEventConsumerMapper transactionMapper;

    @KafkaListener(topics = "${topic.transaction-created}", groupId = "account-service")
    public void consume(TransactionCreatedEvent event) {

        try {
            String transactionId=event.getTransactionId().toString();
            TransactionType transactionType=TransactionType.valueOf(event.getTransactionType().toString());

            log.info("[ACCOUNT] Received TRANSACTION_CREATED txId={}, type={}", transactionId, transactionType);

            if (!processCreatedTransactionUseCase.isAccountServiceResponsible(transactionType)) {
                log.info("[ACCOUNT] Ignoring txId={} type={} (not account responsibility)", transactionId, transactionType);
                return;
            }

            TransactionCommand cmd=new TransactionCommand(
                event.getTransactionId().toString(),
                event.getCustomerId().toString(),
                event.getDescription().toString(),
                TransactionType.valueOf(event.getTransactionType().toString()),
                event.getSourceProductId().toString(),
                event.getTargetProductId().toString(),
                new BigDecimal(event.getAmount())
            );

            processCreatedTransactionUseCase.execute(cmd).subscribe();

        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage());
        }
    }
}
