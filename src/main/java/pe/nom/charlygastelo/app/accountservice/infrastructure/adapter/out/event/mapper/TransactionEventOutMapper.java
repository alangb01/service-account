package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionEventOutMapper {

//    public TransactionCompletedEvent toTransactionCompletedEvent(Transaction transaction) {
//        return TransactionCompletedEvent.newBuilder()
//                .setEventId(UUID.randomUUID().toString())
//                .setEventType("TRANSACTION_COMPLETED")
//                .setOccurredAt(Instant.now().toString())
//                .setVersion("1.0")
//                .setSource("account-service")
//
//                .setTransactionId(value(transaction.id()))
//                .setCustomerId(value(transaction.customerId()))
//                .setStatus("COMPLETED")
//                .setAmount(Double.parseDouble(transaction.amount().toString()))
//
//                .build();
//    }

    public TransactionFailedEvent toTransactionFailedEvent(String transactionId, String customerId, String reason) {
        return TransactionFailedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("TRANSACTION_FAILED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(value(transactionId))
                .setCustomerId(value(customerId))
                .setReason(reason)

                .build();
    }



    private String value(String value) {
        return value == null ? "" : value;
    }
}