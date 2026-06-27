package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.time.Instant;
import java.util.UUID;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.MovementEventPort;
import pe.nom.charlygastelo.app.shared.avro.dto.MovementRegisterRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovementKafkaClient implements MovementEventPort {

    private final MovementRegisterRequestProducer producer;

    @Override
    public Completable registerMovement(
            Transaction transaction,
            Account account,
            String movementType) {

        return Completable.fromRunnable(() -> {
            String correlationId = UUID.randomUUID().toString();

            MovementRegisterRequestEvent event =
                    MovementRegisterRequestEvent.newBuilder()
                            .setEventId(UUID.randomUUID().toString())
                            .setEventType("MOVEMENT_REGISTER_REQUEST")
                            .setOccurredAt(Instant.now().toString())
                            .setVersion("1.0")
                            .setSource("account-service")
                            .setCorrelationId(correlationId)
                            .setCustomerId(transaction.customerId())
                            .setProductId(account.id())
                            .setProductType("ACCOUNT")
                            .setMovementType(movementType)
                            .setAmount(transaction.amount().doubleValue())
                            .setBalanceAfter(account.balance().doubleValue())
                            .setTransactionId(transaction.id())
                            .setDescription(transaction.description() == null ? "" : transaction.description())
                            .build();

            producer.send(correlationId, event);

            log.info("MovementRegisterRequestEvent sent. correlationId={}, transactionId={}",
                    correlationId, transaction.id());
        });
    }
}