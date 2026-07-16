package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessWithdrawUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.shared.avro.dto.DebitCardPaymentOccurredEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebitCardPaymentConsumer {
    private final ProcessWithdrawUseCase withDrawUseCase;

    @KafkaListener(
            topics = "${topic.debit-card-payment-occurred}",
            groupId = "account-service"
    )
    public void consume(DebitCardPaymentOccurredEvent event) {
        log.info("[ACCOUNT] Received DEBIT_CARD_PAYMENT_OCCURRED. txId={}, cardId={}, accountId={}, customerId={} amount={}",
                event.getTransactionId(), event.getCardId(), event.getAccountId(), event.getCustomerId(), event.getAmount());

        Transaction tx=new Transaction(
                event.getTransactionId().toString(),
                event.getCustomerId().toString(),
                null,
                null,
                event.getAccountId().toString(),
                event.getCardId().toString(),
                TransactionType.WITHDRAW,
                new BigDecimal(event.getAmount()),
                BigDecimal.ZERO,
                "TO DEBIT CARD PAYMENT",
                null
        );

        withDrawUseCase.execute(tx).subscribe();
    }
}
