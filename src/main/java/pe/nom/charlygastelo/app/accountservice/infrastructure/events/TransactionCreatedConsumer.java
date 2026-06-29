package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ProcessTransactionUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedConsumer {

    private static final String PRODUCT_TYPE_ACCOUNT = "ACCOUNT";

    private final AvroJsonDeserializer deserializer;
    private final ProcessTransactionUseCase useCase;

    @KafkaListener(
            topics = "${topic.transaction-created}",
            groupId = "account-service")
    public void consume(String message) {
        log.debug("TransactionCreatedEvent raw message received");

        try {
            TransactionCreatedEvent event =
                    deserializer.deserialize(
                            message,
                            TransactionCreatedEvent.class,
                            TransactionCreatedEvent.getClassSchema()
                    );

            String transactionId = event.getTransactionId().toString();
            String sourceProductType =
                    event.getSourceProductType() == null
                            ? null
                            : event.getSourceProductType().toString();

            String targetProductType =
                    event.getTargetProductType() == null
                            ? null
                            : event.getTargetProductType().toString();


            log.info(
                    "TransactionCreatedEvent received. transactionId={}, type={}, sourceProductType={}, targetProductType={}",
                    transactionId,
                    event.getTransactionType(),
                    sourceProductType,
                    targetProductType
            );

            boolean supportedByAccountService =
                    "DEPOSIT".equalsIgnoreCase(event.getTransactionType().toString())
                            || "WITHDRAWAL".equalsIgnoreCase(event.getTransactionType().toString())
                            || "TRANSFER".equalsIgnoreCase(event.getTransactionType().toString())
                            || "DEBIT_CARD_PAYMENT".equalsIgnoreCase(event.getTransactionType().toString());

            if (!supportedByAccountService) {
                log.info("Transaction ignored by account-service. transactionId={}, type={}",
                        transactionId, event.getTransactionType());
                return;
            }

            String sourceProductId =
                    event.getSourceProductId() == null
                            ? null
                            : event.getSourceProductId().toString();

            String targetProductId =
                    event.getTargetProductId() == null
                            ? null
                            : event.getTargetProductId().toString();

            Transaction transaction = new Transaction(
                    transactionId,
                    event.getCustomerId().toString(),
                    sourceProductId,
                    targetProductId,
                    TransactionType.valueOf(event.getTransactionType().toString()),
                    BigDecimal.valueOf(event.getAmount()),
                    BigDecimal.valueOf(event.getCommission()),
                    event.getDescription() == null ? "" : event.getDescription().toString()
            );

            useCase.execute(transaction)
                    .subscribe(
                            () -> log.info(
                                    "Transaction processed successfully by account-service. transactionId={}",
                                    transactionId
                            ),
                            error -> log.error(
                                    "Error processing transaction in account-service. transactionId={}, reason={}",
                                    transactionId,
                                    error.getMessage(),
                                    error
                            )
                    );

        } catch (Exception e) {
            log.error(
                    "Error consuming TransactionCreatedEvent. reason={}",
                    e.getMessage(),
                    e
            );
        }
    }
}