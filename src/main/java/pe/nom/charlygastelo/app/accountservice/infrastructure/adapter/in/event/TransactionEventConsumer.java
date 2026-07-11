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
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountRequestEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final ProcessCreatedTransactionUseCase processCreatedTransactionUseCase;
    private final TransactionEventConsumerMapper transactionMapper;
    private final TransactionEventProducer transactionEventProducer;

    @KafkaListener(topics = "${topic.transaction-created}", groupId = "account-service")
    public void consumeTransactionCreatedRequest(TransactionCreatedEvent event) {
        try{


            String correlationId = event.getEventId().toString();
            String sourceId = event.getSourceProductId().toString();
            String targetId = event.getTargetProductId().toString();
            BigDecimal amount = BigDecimal.valueOf(event.getAmount());

            log.info("[ACCOUNT-TRANSFER] Processing transfer source={}, target={}, amount={}, correlationId={}",
                    sourceId, targetId, amount, correlationId);

            Transaction transaction=transactionMapper.toDomain(event);

            log.info("[TRANSACTION-CREATED] Event deserialized successfully. correlationId={}, customerId={}",
                    correlationId, transaction.customerId());

            processCreatedTransactionUseCase.execute(transaction, correlationId)
                    .subscribe(
                        () -> log.info("[TX-DEPOSIT] Completed txId={}, correlationId={}", transaction.id(), correlationId),
                    error -> {
                                    log.error("[TX-DEPOSIT] Failed txId={}, correlationId={}, reason={}",
                                            transaction.id(), correlationId, error.getMessage());

                                    transactionEventProducer.publishTransactionFailed(transaction, error.getMessage())
                                        .subscribe(
                                                () -> log.info("[TX-DEPOSIT] FAILED event published txId={}, correlationId={}",
                                                        transaction.id(), correlationId),
                                                err2 -> log.error("[TX-DEPOSIT] FAILED event could not be published txId={}, reason={}",
                                                        transaction.id(), err2.getMessage())
                                        );
                        }
                    );
        } catch (Exception ex){
            log.error(ex.getMessage());
        }
    }
}
